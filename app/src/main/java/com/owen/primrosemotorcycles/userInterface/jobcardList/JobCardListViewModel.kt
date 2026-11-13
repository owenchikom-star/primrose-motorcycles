package com.owen.primrosemotorcycles.userInterface.jobcardList

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.firestore.FirebaseFirestore
import com.owen.primrosemotorcycles.data.CheckupDao
import com.owen.primrosemotorcycles.data.Invoice
import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.data.JobCardDao
import com.owen.primrosemotorcycles.data.JobStatus
import com.owen.primrosemotorcycles.data.Quotation
import com.owen.primrosemotorcycles.data.QuotationItem
import com.owen.primrosemotorcycles.data.SparePart
import com.owen.primrosemotorcycles.data.SparePartDao
import com.owen.primrosemotorcycles.userInterface.invoice.InvoiceItemUiState
import com.owen.primrosemotorcycles.userInterface.invoice.InvoicePdfGenerator
import com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails.JobCardPdfGenerator
import com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails.RequisitionPdfGenerator
import com.owen.primrosemotorcycles.worker.JobCardStatusWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class JobListUiState(
    val jobs: List<JobCard> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: JobStatus? = null,
    val openCount: Int = 0,
    val inProgressCount: Int = 0,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val branchId: String = "germiston_main",
    val errorMsg: String? = null,
    val selectedJobId: String? = null,
    val isCreating: Boolean = false,
    val detailDestination: String? = null
)

data class CreateUiState(
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val jobId: String? = null,
    val currentJobId: String = UUID.randomUUID().toString(),
    val jobNumber: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val customerAddress: String = "",
    val bikeReg: String = "",
    val bikeModel: String = "",
    val bikeColor: String = "",
    val mileage: String = "",
    val vinNo: String = "",
    val engNo: String = "",
    val workshopNotes: String = "",
    val workshopRepairs: String = "",
    val sparesMaterials: String = "",
    val customerInstructions: String = "",
    val issue: String = "",
    val technician: String = "",
    val customerSignature: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class QuotationItemUiState(
    val quotationId: String = "",
    val description: String = "",
    val quantity: Double = 0.0,
    val inclPrice: Double = 0.0,
    val sortOrder: Int = 0
) {
    val exclPrice: Double get() = inclPrice / 1.15
    val totalExcl: Double get() = exclPrice * quantity
    val totalIncl: Double get() = inclPrice * quantity
}
data class QuotationUiState(
    val reference: String = "",
    val salesRep: String = "",
    val stockNo: String = "",
    val customerNotes: String = "",
    val showAddItemDialog: Boolean = false,
    val itemDescription: String = "",
    val itemQuantity: String = "1",
    val itemPrice: String = ""
)

data class InvoiceUiState(
    val reference: String = "",
    val salesRep: String = "",
    val stockNo: String = "",
    val customerNotes: String = "",
    val showAddItemDialog: Boolean = false,
    val itemDescription: String = "",
    val itemQuantity: String = "1",
    val itemPrice: String = ""
)

data class SparesUiState(
    val showDialog: Boolean = false,
    val editingPart: SparePart? = null,
    val name: String = "",
    val partNumber: String = "",
    val quantity: String = "1",
    val unitPrice: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JobCardListViewModel @Inject constructor(
    val dao: JobCardDao,
    val sparePartDao: SparePartDao,
    val checkupDao: CheckupDao,
    val firestore: FirebaseFirestore,
    @ApplicationContext val context: Context
) : ViewModel() {

    private fun scheduleStatusCheck(jobCardId: String) {
        val ctx = context ?: return
        val workRequest = OneTimeWorkRequestBuilder<JobCardStatusWorker>()
            .setInitialDelay(20, TimeUnit.MINUTES)
            .setInputData(workDataOf("JOB_CARD_ID" to jobCardId))
            .build()

        WorkManager.getInstance(ctx).enqueueUniqueWork(
            "JobCardStatus_$jobCardId",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    init {
        viewModelScope.launch {
            try {
                dao.fixBranchIds()
            } catch (_: Exception) {}
        }
    }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatus = MutableStateFlow<JobStatus?>(null)
    private val _branchId = MutableStateFlow("germiston_main")
    private val _isSyncing = MutableStateFlow(false)
    private val _errorMsg = MutableStateFlow<String?>(null)
    private val _tabletSelectedJobId = MutableStateFlow<String?>(null)
    private val _tabletIsCreating = MutableStateFlow(false)
    private val _tabletDetailDestination = MutableStateFlow<String?>(null)

    fun selectJobCard(id: String?) {
        _tabletSelectedJobId.value = id
        _tabletIsCreating.value = false
        _tabletDetailDestination.value = null
    }

    fun setCreating(creating: Boolean) {
        _tabletIsCreating.value = creating
        _tabletSelectedJobId.value = null
        _tabletDetailDestination.value = null
    }

    fun setDetailDestination(destination: String?) {
        _tabletDetailDestination.value = destination
    }

    private val _createUiState = MutableStateFlow(CreateUiState())
    val createUiState: StateFlow<CreateUiState> = _createUiState.asStateFlow()

    private val jobsFlow: Flow<List<JobCard>> = combine(_searchQuery, _branchId) { q, b -> q to b }
        .flatMapLatest { (query, branch) ->
            if (query.isBlank()) dao.getAll(branch) else dao.search(branch, query)
        }

    val uiState: StateFlow<JobListUiState> = combine(
        jobsFlow,
        _searchQuery,
        _selectedStatus,
        dao.countByStatusFlow("germiston_main", JobStatus.OPEN),
        dao.countByStatusFlow("germiston_main", JobStatus.IN_PROGRESS),
        _isSyncing,
        _errorMsg,
        _tabletSelectedJobId,
        _tabletIsCreating,
        _tabletDetailDestination
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val jobs = args[0] as List<JobCard>
        val query = args[1] as String
        val status = args[2] as JobStatus?
        val open = args[3] as Int
        val inProg = args[4] as Int
        val syncing = args[5] as Boolean
        val error = args[6] as String?
        val selId = args[7] as String?
        val creating = args[8] as Boolean
        val dest = args[9] as String?

        val filtered = if (status == null) jobs else jobs.filter { it.status == status.name }

        JobListUiState(
            jobs = filtered,
            searchQuery = query,
            selectedStatus = status,
            openCount = open,
            inProgressCount = inProg,
            isLoading = false,
            isSyncing = syncing,
            errorMsg = error,
            selectedJobId = selId,
            isCreating = creating,
            detailDestination = dest
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JobListUiState())

    // --- UI Events ---
    fun onSearchChange(query: String) { _searchQuery.value = query }
    fun onStatusFilterChange(status: JobStatus?) {
        _selectedStatus.value = if (_selectedStatus.value == status) null else status
    }
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedStatus.value = null
    }

    fun resetForNewJobCard() {
        viewModelScope.launch {
            val count = dao.count("germiston_main") + 1
            val newNumber = count.toString().padStart(6, '0')
            val newId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            _createUiState.value = CreateUiState(
                isEditMode = false,
                jobId = null,
                currentJobId = newId,
                jobNumber = newNumber,
                createdAt = now
            )
            _sparesUiState.value = SparesUiState()
            seedDefaultSparesForJob(newId)
        }
    }

    suspend fun loadJobForEdit(jobId: String?) {
        if (jobId.isNullOrBlank()) {
            initNewIfNeeded()
            return
        }
        if (_createUiState.value.isEditMode && _createUiState.value.jobId == jobId) return
        viewModelScope.launch {
            val existing = dao.getById(jobId)
            if (existing != null) {
                _createUiState.value = CreateUiState(
                    isEditMode = true,
                    jobId = existing.id,
                    currentJobId = existing.id,
                    jobNumber = existing.jobNumber,
                    customerName = existing.customerName,
                    customerPhone = existing.customerPhone,
                    customerAddress = existing.customerAddress,
                    driverName = existing.driver,
                    driverPhone = existing.driverPhone,
                    bikeReg = existing.bikeReg,
                    bikeModel = existing.bikeModel,
                    bikeColor = existing.bikeColor,
                    mileage = existing.mileage,
                    vinNo = existing.vinNo,
                    engNo = existing.engNo,
                    workshopNotes = existing.workshopNotes,
                    workshopRepairs = existing.workshopRepairs,
                    sparesMaterials = existing.sparesList,
                    customerInstructions = existing.customerInstructions,
                    issue = existing.reportedIssue,
                    customerSignature = existing.customerSignature,
                    technician = existing.technician,
                    createdAt = existing.createdAt
                )
            } else {
                resetForNewJobCard()
            }
        }
    }

    fun initNewIfNeeded() {
        val currentState = _createUiState.value
        if (currentState.isEditMode || currentState.jobId != null || currentState.currentJobId.isBlank()) {
            resetForNewJobCard()
        }
    }

    private suspend fun seedDefaultSparesForJob(jobId: String) {
        val existingSpares = sparePartDao.getForJobSync(jobId)
        if (existingSpares.isEmpty()) {
            sparePartDao.insert(
                SparePart(
                    jobCardId = jobId,
                    name = "CONSUMABLES",
                    partNumber = "W/SHOPCONS2",
                    quantity = 1,
                    unitPrice = 180.0
                )
            )
            sparePartDao.insert(
                SparePart(
                    jobCardId = jobId,
                    name = "WORKSHOP LABOUR",
                    partNumber = "W/SHOPLABOUR",
                    quantity = 1,
                    unitPrice = 2880.0
                )
            )
            updateJobCardSparesTotal(jobId)
        }
    }

    // Add this state
    private val _quotationItems = MutableStateFlow<List<QuotationItemUiState>>(emptyList())
    val quotationItems: StateFlow<List<QuotationItemUiState>> = _quotationItems

    private val _quotationUiState = MutableStateFlow(QuotationUiState())
    val quotationUiState: StateFlow<QuotationUiState> = _quotationUiState.asStateFlow()

    private val _invoiceItems = MutableStateFlow<List<InvoiceItemUiState>>(emptyList())
    val invoiceItems: StateFlow<List<InvoiceItemUiState>> = _invoiceItems

    private val _invoiceUiState = MutableStateFlow(InvoiceUiState())
    val invoiceUiState: StateFlow<InvoiceUiState> = _invoiceUiState.asStateFlow()

    private val _sparesUiState = MutableStateFlow(SparesUiState())
    val sparesUiState: StateFlow<SparesUiState> = _sparesUiState.asStateFlow()

    fun initQuotationStateForJob() {
        val s = _createUiState.value
        _quotationUiState.update {
            it.copy(
                reference = "${s.bikeReg} ",
                salesRep = "",
                stockNo = "",
                customerNotes = ""
            )
        }
    }

    fun updateQuotationReference(text: String) { _quotationUiState.update { it.copy(reference = text) } }
    fun updateQuotationSalesRep(text: String) { _quotationUiState.update { it.copy(salesRep = text) } }
    fun updateQuotationStockNo(text: String) { _quotationUiState.update { it.copy(stockNo = text) } }
    fun updateQuotationCustomerNotes(text: String) { _quotationUiState.update { it.copy(customerNotes = text) } }

    fun setQuotationAddItemDialogVisible(visible: Boolean) {
        _quotationUiState.update {
            it.copy(
                showAddItemDialog = visible,
                itemDescription = "",
                itemQuantity = "1",
                itemPrice = ""
            )
        }
    }
    fun updateQuotationItemDescription(text: String) { _quotationUiState.update { it.copy(itemDescription = text) } }
    fun updateQuotationItemQuantity(text: String) { _quotationUiState.update { it.copy(itemQuantity = text) } }
    fun updateQuotationItemPrice(text: String) { _quotationUiState.update { it.copy(itemPrice = text) } }

    fun confirmAddQuotationItem(targetJobId: String? = null) {
        val qState = _quotationUiState.value
        val desc = qState.itemDescription.trim()
        val qty = qState.itemQuantity.toDoubleOrNull() ?: 1.0
        val price = qState.itemPrice.toDoubleOrNull() ?: 0.0
        if (desc.isNotBlank()) {
            addQuotationItem(desc, qty, price, targetJobId)
        }
        setQuotationAddItemDialogVisible(false)
    }

    // Invoice Actions
    fun initInvoiceStateForJob() {
        val s = _createUiState.value
        _invoiceUiState.update {
            it.copy(
                reference = "${s.bikeReg} INV",
                salesRep = "",
                stockNo = "",
                customerNotes = ""
            )
        }
    }

    fun updateInvoiceReference(text: String) { _invoiceUiState.update { it.copy(reference = text) } }
    fun updateInvoiceSalesRep(text: String) { _invoiceUiState.update { it.copy(salesRep = text) } }
    fun updateInvoiceStockNo(text: String) { _invoiceUiState.update { it.copy(stockNo = text) } }
    fun updateInvoiceCustomerNotes(text: String) { _invoiceUiState.update { it.copy(customerNotes = text) } }

    fun setInvoiceAddItemDialogVisible(visible: Boolean) {
        _invoiceUiState.update {
            it.copy(
                showAddItemDialog = visible,
                itemDescription = "",
                itemQuantity = "1",
                itemPrice = ""
            )
        }
    }
    fun updateInvoiceItemDescription(text: String) { _invoiceUiState.update { it.copy(itemDescription = text) } }
    fun updateInvoiceItemQuantity(text: String) { _invoiceUiState.update { it.copy(itemQuantity = text) } }
    fun updateInvoiceItemPrice(text: String) { _invoiceUiState.update { it.copy(itemPrice = text) } }

    fun confirmAddInvoiceItem(targetJobId: String? = null) {
        val state = _invoiceUiState.value
        val desc = state.itemDescription.trim()
        val qty = state.itemQuantity.toDoubleOrNull() ?: 1.0
        val price = state.itemPrice.toDoubleOrNull() ?: 0.0
        if (desc.isNotBlank()) {
            addInvoiceItem(desc, qty, price, targetJobId)
        }
        setInvoiceAddItemDialogVisible(false)
    }

    fun loadSparesIntoInvoice(targetJobId: String? = null) {
        viewModelScope.launch {
            val idToUse = if (!targetJobId.isNullOrBlank()) targetJobId else (_createUiState.value.jobId ?: _createUiState.value.currentJobId)
            if (idToUse.isBlank()) return@launch
            sparePartDao.getForJob(idToUse).collect { spares ->
                _invoiceItems.value = spares.mapIndexed { index, sp ->
                    InvoiceItemUiState(
                        invoiceId = "",
                        description = if (sp.partNumber.isNotBlank()) "${sp.partNumber} - ${sp.name}" else "SI. - ${sp.name.uppercase()}",
                        quantity = sp.quantity.toDouble(),
                        inclPrice = sp.unitPrice,
                        sortOrder = index + 10
                    )
                }
            }
        }
    }

    fun addInvoiceItem(description: String, quantity: Double, inclPrice: Double, targetJobId: String? = null) {
        val idToUse = if (!targetJobId.isNullOrBlank()) targetJobId else (_createUiState.value.jobId ?: _createUiState.value.currentJobId)
        viewModelScope.launch {
            if (idToUse.isNotBlank()) {
                val spare = SparePart(
                    jobCardId = idToUse,
                    name = description,
                    partNumber = "",
                    quantity = quantity.toInt().coerceAtLeast(1),
                    unitPrice = inclPrice
                )
                sparePartDao.insert(spare)
                updateJobCardSparesTotal(idToUse)
            }
        }
    }

    fun generateInvoicePdf(context: Context, targetJobId: String?, onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            val idToUse = if (!targetJobId.isNullOrBlank()) targetJobId else (_createUiState.value.jobId ?: _createUiState.value.currentJobId)
            val job = dao.getById(idToUse) ?: JobCard(id = idToUse)
            val state = _invoiceUiState.value
            val invoice = Invoice(
                number = "INV${(10000..99999).random()}",
                reference = state.reference,
                salesRep = state.salesRep,
                stockNo = state.stockNo,
                customerNotes = state.customerNotes,
                jobCardId = idToUse,
                customerName = job.customerName,
                vehicle = job.bikeModel,
                regNo = job.bikeReg,
                km = job.mileage,
                engineNo = job.engNo,
                vinNo = job.vinNo
            )
            val generator = InvoicePdfGenerator(context)
            val file = generator.generate(invoice, job, _invoiceItems.value)
            onFileReady(file)
        }
    }

    // SparesSection State Actions
    fun openAddSpareDialog() {
        _sparesUiState.value = SparesUiState(
            showDialog = true,
            editingPart = null,
            name = "",
            partNumber = "",
            quantity = "1",
            unitPrice = ""
        )
    }

    fun openEditSpareDialog(part: SparePart) {
        _sparesUiState.value = SparesUiState(
            showDialog = true,
            editingPart = part,
            name = part.name,
            partNumber = part.partNumber,
            quantity = part.quantity.toString(),
            unitPrice = if (part.unitPrice > 0.0) part.unitPrice.toString() else ""
        )
    }

    fun closeSpareDialog() {
        _sparesUiState.update { it.copy(showDialog = false, editingPart = null) }
    }

    fun updateSpareName(text: String) { _sparesUiState.update { it.copy(name = text) } }
    fun updateSparePartNumber(text: String) { _sparesUiState.update { it.copy(partNumber = text) } }
    fun updateSpareQuantity(text: String) { _sparesUiState.update { it.copy(quantity = text) } }
    fun updateSpareUnitPrice(text: String) { _sparesUiState.update { it.copy(unitPrice = text) } }

    fun confirmSaveSpare(jobCardId: String) {
        val s = _sparesUiState.value
        val name = s.name.trim()
        val partNo = s.partNumber.trim()
        val qty = s.quantity.toIntOrNull() ?: 1
        val price = s.unitPrice.toDoubleOrNull() ?: 0.0
        val editing = s.editingPart

        if (name.isNotBlank()) {
            if (editing != null) {
                updateSpare(
                    editing.copy(
                        name = name,
                        partNumber = partNo,
                        quantity = qty,
                        unitPrice = price
                    )
                )
            } else {
                addSpare(jobCardId, name, partNo, qty, price)
            }
        }
        closeSpareDialog()
    }

    fun prepareQuotationFromCurrentJob(targetJobId: String? = null): Quotation {
        val s = _createUiState.value
        val idToUse = if (!targetJobId.isNullOrBlank()) targetJobId else (s.jobId ?: s.currentJobId)
        return Quotation(
            number = "QUO${System.currentTimeMillis().toString().takeLast(6)}",
            reference = "${s.bikeReg} S",
            jobCardId = idToUse,
            vehicle = s.bikeModel,
            regNo = s.bikeReg,
            km = s.mileage,
            driver = s.driverName,
            stockNo = "",
            engineNo = s.engNo,
            vinNo = s.vinNo,
            customerName = s.customerName,
            customerNotes = ""
        )
    }

    fun loadSparesIntoQuotation(targetJobId: String? = null) {
        viewModelScope.launch {
            val idToUse = if (!targetJobId.isNullOrBlank()) targetJobId else (_createUiState.value.jobId ?: _createUiState.value.currentJobId)
            if (idToUse.isBlank()) return@launch
            sparePartDao.getForJob(idToUse).collect { spares ->
                _quotationItems.value = spares.mapIndexed { index, sp ->
                    QuotationItemUiState(
                        quotationId = "", // set later
                        description = if (sp.partNumber.isNotBlank()) "${sp.partNumber} - ${sp.name}" else "SI. - ${sp.name.uppercase()}",
                        quantity = sp.quantity.toDouble(),
                        inclPrice = sp.unitPrice,
                        sortOrder = index + 10
                    )
                }
            }
        }
    }

    fun addQuotationItem(description: String, quantity: Double, inclPrice: Double, targetJobId: String? = null) {
        val idToUse = if (!targetJobId.isNullOrBlank()) targetJobId else (_createUiState.value.jobId ?: _createUiState.value.currentJobId)
        viewModelScope.launch {
            if (idToUse.isNotBlank()) {
                val spare = SparePart(
                    jobCardId = idToUse,
                    name = description,
                    partNumber = "",
                    quantity = quantity.toInt().coerceAtLeast(1),
                    unitPrice = inclPrice
                )
                sparePartDao.insert(spare)
                updateJobCardSparesTotal(idToUse)
            }
        }
    }


    fun updateCustomerName(text: String) { _createUiState.update { it.copy(customerName = text) } }
    fun updateCustomerPhone(text: String) { _createUiState.update { it.copy(customerPhone = text) } }
    fun updateDriverName(text: String) { _createUiState.update { it.copy(driverName = text) } }
    fun updateDriverPhone(text: String) { _createUiState.update { it.copy(driverPhone = text) } }
    fun updateCustomerAddress(text: String) { _createUiState.update { it.copy(customerAddress = text) } }
    fun updateBikeReg(text: String) { _createUiState.update { it.copy(bikeReg = text.uppercase()) } }
    fun updateBikeModel(text: String) { _createUiState.update { it.copy(bikeModel = text) } }
    fun updateBikeColor(text: String) { _createUiState.update { it.copy(bikeColor = text) } }
    fun updateMileage(text: String) { _createUiState.update { it.copy(mileage = text) } }
    fun updateVinNo(text: String) { _createUiState.update { it.copy(vinNo = text) } }
    fun updateEngNo(text: String) { _createUiState.update { it.copy(engNo = text) } }
    fun updateWorkshopNotes(text: String) { _createUiState.update { it.copy(workshopNotes = text) } }
    fun updateWorkshopRepairs(text: String) { _createUiState.update { it.copy(workshopRepairs = text) } }
    fun updateSparesMaterials(text: String) { _createUiState.update { it.copy(sparesMaterials = text) } }
    fun updateCustomerInstructions(text: String) { _createUiState.update { it.copy(customerInstructions = text) } }
    fun updateIssue(text: String) { _createUiState.update { it.copy(issue = text) } }
    fun updateTechnician(text: String) { _createUiState.update { it.copy(technician = text) } }
    fun updateCustomerSignature(text: String) { _createUiState.update { it.copy(customerSignature = text) } }

    fun saveCurrentJobCard(onSaved: () -> Unit) {
        val state = _createUiState.value
        val card = JobCard(
            id = state.jobId ?: state.currentJobId,
            jobNumber = state.jobNumber,
            branchId = "germiston_main",
            customerName = state.customerName.trim(),
            customerPhone = state.customerPhone.trim(),
            customerAddress = state.customerAddress.trim(),
            driver = state.driverName.trim(),
            driverPhone = state.driverPhone.trim(),
            bikeReg = state.bikeReg.trim(),
            bikeModel = state.bikeModel.trim(),
            bikeColor = state.bikeColor.trim(),
            mileage = state.mileage.trim(),
            vinNo = state.vinNo.trim(),
            engNo = state.engNo.trim(),
            workshopNotes = state.workshopNotes.trim(),
            workshopRepairs = state.workshopRepairs.trim(),
            sparesList = state.sparesMaterials.trim(),
            customerInstructions = state.customerInstructions.trim(),
            reportedIssue = state.issue.trim(),
            technician = state.technician.trim(),
            customerSignature = state.customerSignature,
            status = JobStatus.OPEN.name,
            createdAt = state.createdAt
        )
        saveToBoth(card, onSaved)
    }

    fun generateCurrentJobCardPdf(context: Context, onGenerated: (File) -> Unit) {
        val state = _createUiState.value
        val targetId = state.jobId ?: state.currentJobId
        saveCurrentJobCard {
            generateJobCardPdf(context, targetId, onGenerated)
        }
    }

    private suspend fun updateJobCardStatusToInProgressIfNeeded(jobCardId: String) {
        val job = dao.getById(jobCardId) ?: return
        val currentStatus = job.getStatusEnum()
        if (currentStatus == JobStatus.OPEN || currentStatus == JobStatus.WAITING_PARTS) {
            dao.updateStatus(jobCardId, JobStatus.IN_PROGRESS)
            try {
                firestore?.collection("branches")?.document(job.branchId)
                    ?.collection("jobCards")?.document(jobCardId)
                    ?.update("status", JobStatus.IN_PROGRESS.name)
                    ?.await()
            } catch (_: Exception) {}
        }
    }

    fun addSpare(jobCardId: String, name: String, partNo: String, qty: Int, price: Double) {
        viewModelScope.launch {
            sparePartDao.insert(SparePart(jobCardId = jobCardId, name = name, partNumber = partNo, quantity = qty, unitPrice = price))
            updateJobCardStatusToInProgressIfNeeded(jobCardId)
            updateJobCardSparesTotal(jobCardId)
        }
    }

    fun updateSpare(part: SparePart) {
        viewModelScope.launch {
            sparePartDao.insert(part)
            updateJobCardStatusToInProgressIfNeeded(part.jobCardId)
            updateJobCardSparesTotal(part.jobCardId)
        }
    }

    fun deleteSpare(part: SparePart) {
        viewModelScope.launch {
            sparePartDao.delete(part)
            updateJobCardSparesTotal(part.jobCardId)
        }
    }

    private suspend fun updateJobCardSparesTotal(jobCardId: String) {
        val total = sparePartDao.getTotalForJobSync(jobCardId) ?: 0.0
        val job = dao.getById(jobCardId) ?: return
        val rands = total.toInt()
        val cents = ((total - rands) * 100).toInt()
        val centsStr = cents.toString().padStart(2, '0')
        val updated = job.copy(
            sparesR = rands.toString(),
            sparesC = centsStr,
            finalTotalR = rands.toString(),
            finalTotalC = centsStr,
            finalTotal = String.format(java.util.Locale.US, "R %.2f", total)
        )
        dao.insert(updated)
        try {
            firestore?.collection("branches")?.document(updated.branchId)
                ?.collection("jobCards")?.document(updated.id)
                ?.set(updated)?.await()
        } catch (_: Exception) {}
    }

    fun generateRequisitionPdf(context: Context, jobId: String, onGenerated: (File) -> Unit) {
        viewModelScope.launch {
            val job = dao.getById(jobId) ?: return@launch
            val parts = sparePartDao.getForJobSync(jobId)
            val file = RequisitionPdfGenerator(context).generate(job, parts)
            onGenerated(file)
        }
    }

    fun generateJobCardPdf(context: Context, jobId: String, onGenerated: (File) -> Unit) {
        viewModelScope.launch {
            val job = dao.getById(jobId) ?: return@launch
            val parts = sparePartDao.getForJobSync(jobId)
            val file = JobCardPdfGenerator(context).generate(job, parts = parts)
            onGenerated(file)
        }
    }

    fun clearError() { _errorMsg.value = null }

    // --- ROOM + FIRESTORE LOGIC ---

    // Used by Create Screen - saves to both
    fun saveToBoth(jobCard: JobCard, onDone: () -> Unit) {
        if (jobCard.customerName.isBlank()) {
            _errorMsg.value = "Customer name is required"
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val cardToSave = if (jobCard.branchId.isBlank()) jobCard.copy(branchId = "germiston_main") else jobCard

                // 1. Save to Room first (offline safe)
                dao.insert(cardToSave.copy(isSynced = false))

                if (cardToSave.getStatusEnum() == JobStatus.OPEN) {
                    scheduleStatusCheck(cardToSave.id)
                }

                // 2. Save to Firestore: branches/{branchId}/jobCards/{id}
                firestore?.collection("branches")
                    ?.document(cardToSave.branchId)
                    ?.collection("jobCards")
                    ?.document(cardToSave.id)
                    ?.set(cardToSave.copy(isSynced = true))
                    ?.await()

                // 3. Mark synced in Room
                dao.markSynced(cardToSave.id)
                onDone()
            } catch (e: Exception) {
                // Room saved, Firestore failed - will sync later
                _errorMsg.value = "Saved offline. Will sync when online: ${e.message}"
                onDone()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun deleteJobCard(jobCard: JobCard) {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                // 1. Delete from Room immediately
                dao.deleteById(jobCard.id)

                // 2. Delete from Firestore
                firestore?.collection("branches")
                    ?.document(jobCard.branchId)
                    ?.collection("jobCards")
                    ?.document(jobCard.id)
                    ?.delete()
                    ?.await()
            } catch (e: Exception) {
                _errorMsg.value = "Delete offline only: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // Swipe to change status
    fun updateStatus(id: String, status: JobStatus) {
        viewModelScope.launch {
            dao.updateStatus(id, status)
            // Firestore partial update
            try {
                val card = dao.getById(id)
                if (card != null) {
                    firestore?.collection("branches")?.document(card.branchId)
                        ?.collection("jobCards")?.document(id)
                        ?.update("status", status.name)
                        ?.await()
                }
            } catch (_: Exception) {}
        }
    }

    // Retry unsynced on app open
    fun syncUnsynced() {
        viewModelScope.launch {
            val unsynced = dao.getUnsynced()
            if (unsynced.isEmpty()) return@launch
            _isSyncing.value = true
            try {
                for (card in unsynced) {
                    firestore?.collection("branches")?.document(card.branchId)
                        ?.collection("jobCards")?.document(card.id)
                        ?.set(card)?.await()
                    dao.markSynced(card.id)
                }
            } catch (e: Exception) {
                _errorMsg.value = "Sync failed: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }
}

