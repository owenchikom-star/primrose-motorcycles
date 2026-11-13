package com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.data.JobCardDao
import com.owen.primrosemotorcycles.data.SparePart
import com.owen.primrosemotorcycles.data.SparePartDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JobCardDetailViewModel @Inject constructor(
    private val dao: JobCardDao,
    private val sparePartDao: SparePartDao,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _jobId = MutableStateFlow("")
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    val jobCard: StateFlow<JobCard?> = _jobId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null) else dao.getByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val spareParts: StateFlow<List<SparePart>> = _jobId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else sparePartDao.getForJob(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(id: String) { _jobId.value = id }

    fun updateField(updater: (JobCard) -> JobCard) {
        viewModelScope.launch {
            val current = dao.getById(_jobId.value) ?: return@launch
            val updated = updater(current)
            dao.insert(updated.copy(isSynced = false))
            // Sync to Firestore background
            try {
                firestore?.collection("branches")?.document(updated.branchId)
                    ?.collection("jobCards")?.document(updated.id)
                    ?.set(updated)?.await()
                dao.markSynced(updated.id)
            } catch (_: Exception) {}
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

    val jobCards: StateFlow<JobCard?> = _jobId
        .mapLatest { id ->
            if (id.isBlank()) null else dao.getById(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

}
