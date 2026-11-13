package com.owen.primrosemotorcycles.userInterface.jobcardList

import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.data.JobCardDao
import com.owen.primrosemotorcycles.data.JobStatus
import com.owen.primrosemotorcycles.data.SparePart
import com.owen.primrosemotorcycles.data.SparePartDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobCardListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeJobCardDao
    private lateinit var fakeSpareDao: FakeSparePartDao
    private lateinit var viewModel: JobCardListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeJobCardDao()
        fakeSpareDao = FakeSparePartDao()
        viewModel = JobCardListViewModel(fakeDao, fakeSpareDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun resetForNewJobCard_createsCleanStateAndSeedsDefaultSpares() = runTest(testDispatcher) {
        viewModel.resetForNewJobCard()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.createUiState.value
        assertFalse(state.isEditMode)
        assertNull(state.jobId)
        assertTrue(state.currentJobId.isNotBlank())
        assertEquals("000001", state.jobNumber)
        assertEquals("", state.customerName)
        assertEquals("", state.bikeReg)

        val seededSpares = fakeSpareDao.getForJobSync(state.currentJobId)
        assertEquals(2, seededSpares.size)
        assertTrue(seededSpares.any { it.name == "CONSUMABLES" })
        assertTrue(seededSpares.any { it.name == "WORKSHOP LABOUR" })
    }

    @Test
    fun loadJobForEdit_existingJobCard_populatesUiState() = runTest(testDispatcher) {
        val existingCard = JobCard(
            id = "jc-100",
            jobNumber = "000100",
            customerName = "Jane Doe",
            customerPhone = "0821234567",
            bikeReg = "GP123456",
            bikeModel = "Kawasaki Ninja",
            status = "OPEN"
        )
        fakeDao.insert(existingCard)

        viewModel.loadJobForEdit("jc-100")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.createUiState.value
        assertTrue(state.isEditMode)
        assertEquals("jc-100", state.jobId)
        assertEquals("jc-100", state.currentJobId)
        assertEquals("Jane Doe", state.customerName)
        assertEquals("0821234567", state.customerPhone)
        assertEquals("GP123456", state.bikeReg)
        assertEquals("Kawasaki Ninja", state.bikeModel)
    }

    @Test
    fun loadJobForEdit_nullJobIdAfterEdit_resetsToNewJobCard() = runTest(testDispatcher) {
        // Step 1: Load existing job card
        val existingCard = JobCard(
            id = "jc-200",
            customerName = "John Smith",
            bikeReg = "XYZ 999"
        )
        fakeDao.insert(existingCard)
        viewModel.loadJobForEdit("jc-200")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("John Smith", viewModel.createUiState.value.customerName)

        // Step 2: Switch to new job card creation (jobId = null)
        viewModel.loadJobForEdit(null)
        testDispatcher.scheduler.advanceUntilIdle()

        val newState = viewModel.createUiState.value
        assertFalse(newState.isEditMode)
        assertNull(newState.jobId)
        assertEquals("", newState.customerName)
        assertEquals("", newState.bikeReg)
    }

    @Test
    fun customerAndBikeDetailUpdaters_modifyStateCorrectly() {
        viewModel.updateCustomerName("Alice")
        viewModel.updateCustomerPhone("0719876543")
        viewModel.updateBikeReg("abc 123 gp")
        viewModel.updateBikeModel("Suzuki Gixxer")
        viewModel.updateCustomerAddress("123 Main St")
        viewModel.updateIssue("Chain noisy")

        val state = viewModel.createUiState.value
        assertEquals("Alice", state.customerName)
        assertEquals("0719876543", state.customerPhone)
        assertEquals("ABC 123 GP", state.bikeReg) // Auto uppercased
        assertEquals("Suzuki Gixxer", state.bikeModel)
        assertEquals("123 Main St", state.customerAddress)
        assertEquals("Chain noisy", state.issue)
    }

    @Test
    fun searchAndFilterActions_updateListUiState() = runTest(testDispatcher) {
        val job = JobCard(id = "j1", bikeModel = "Ninja", branchId = "germiston_main")
        fakeDao.insert(job)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchChange("Ninja")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Ninja", viewModel.uiState.value.searchQuery)

        viewModel.onStatusFilterChange(JobStatus.IN_PROGRESS)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(JobStatus.IN_PROGRESS, viewModel.uiState.value.selectedStatus)

        viewModel.clearFilters()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertNull(viewModel.uiState.value.selectedStatus)
    }

    @Test
    fun addSpare_insertsSparePartAndUpdatesTotals() = runTest(testDispatcher) {
        val jobId = "job-spares-1"
        viewModel.addSpare(
            jobCardId = jobId,
            name = "Spark Plug",
            partNo = "SP-01",
            qty = 4,
            price = 50.0
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val spares = fakeSpareDao.getForJobSync(jobId)
        assertEquals(1, spares.size)
        assertEquals("Spark Plug", spares[0].name)
        assertEquals(4, spares[0].quantity)
        assertEquals(200.0, spares[0].total, 0.001)

        val total = fakeSpareDao.getTotalForJobSync(jobId)
        assertEquals(200.0, total!!, 0.001)
    }
}

class FakeJobCardDao : JobCardDao {
    private val cards = mutableMapOf<String, JobCard>()
    private val cardsFlow = MutableStateFlow<List<JobCard>>(emptyList())

    private fun updateFlow() {
        cardsFlow.value = cards.values.sortedByDescending { it.createdAt }
    }

    override fun getAll(branchId: String): Flow<List<JobCard>> = cardsFlow

    override suspend fun getById(id: String): JobCard? = cards[id]

    override fun getByIdFlow(id: String): Flow<JobCard?> =
        cardsFlow.map { list -> list.find { it.id == id } }

    override fun getByStatus(branchId: String, status: JobStatus): Flow<List<JobCard>> =
        cardsFlow.map { list -> list.filter { it.status == status.name } }

    override fun search(branchId: String, query: String): Flow<List<JobCard>> =
        cardsFlow.map { list ->
            list.filter {
                it.customerName.contains(query, ignoreCase = true) ||
                        it.bikeReg.contains(query, ignoreCase = true) ||
                        it.jobNumber.contains(query, ignoreCase = true)
            }
        }

    override suspend fun count(branchId: String): Int = cards.values.count { it.branchId == branchId }

    override fun countByStatusFlow(branchId: String, status: JobStatus): Flow<Int> =
        cardsFlow.map { list -> list.count { it.status == status.name } }

    override suspend fun fixBranchIds() {}

    override suspend fun insert(jobCard: JobCard) {
        cards[jobCard.id] = jobCard
        updateFlow()
    }

    override suspend fun insertAll(cards: List<JobCard>) {
        cards.forEach { this.cards[it.id] = it }
        updateFlow()
    }

    override suspend fun update(jobCard: JobCard) {
        cards[jobCard.id] = jobCard
        updateFlow()
    }

    override suspend fun updateStatus(id: String, status: JobStatus) {
        cards[id]?.let {
            cards[id] = it.copy(status = status.name)
            updateFlow()
        }
    }

    override suspend fun setChecklistDone(id: String, done: Boolean) {
        cards[id]?.let {
            cards[id] = it.copy(checklistDone = done)
            updateFlow()
        }
    }

    override suspend fun setQuotationDone(id: String, done: Boolean) {
        cards[id]?.let {
            cards[id] = it.copy(quotationDone = done)
            updateFlow()
        }
    }

    override suspend fun getUnsynced(): List<JobCard> = cards.values.filter { !it.isSynced }

    override suspend fun markSynced(id: String) {
        cards[id]?.let { cards[id] = it.copy(isSynced = true) }
    }

    override suspend fun deleteById(id: String) {
        cards.remove(id)
        updateFlow()
    }

    override suspend fun clearBranch(branchId: String) {
        cards.entries.removeIf { it.value.branchId == branchId }
        updateFlow()
    }
}

class FakeSparePartDao : SparePartDao {
    private val parts = mutableMapOf<String, SparePart>()
    private val partsFlow = MutableStateFlow<List<SparePart>>(emptyList())

    private fun updateFlow() {
        partsFlow.value = parts.values.toList()
    }

    override fun getForJob(jobId: String): Flow<List<SparePart>> =
        partsFlow.map { list -> list.filter { it.jobCardId == jobId } }

    override suspend fun getForJobSync(jobId: String): List<SparePart> =
        parts.values.filter { it.jobCardId == jobId }

    override suspend fun insert(part: SparePart) {
        parts[part.id] = part
        updateFlow()
    }

    override suspend fun delete(part: SparePart) {
        parts.remove(part.id)
        updateFlow()
    }

    override fun getTotalForJob(jobId: String): Flow<Double?> =
        partsFlow.map { list ->
            list.filter { it.jobCardId == jobId }.sumOf { it.total }
        }

    override suspend fun getTotalForJobSync(jobId: String): Double? =
        parts.values.filter { it.jobCardId == jobId }.sumOf { it.total }

    override suspend fun getById(id: String): SparePart? = parts[id]

    override suspend fun deleteById(id: String) {
        parts.remove(id)
        updateFlow()
    }
}
