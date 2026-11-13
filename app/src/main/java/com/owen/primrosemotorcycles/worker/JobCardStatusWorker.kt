package com.owen.primrosemotorcycles.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.owen.primrosemotorcycles.data.JobCardDao
import com.owen.primrosemotorcycles.data.JobStatus
import com.owen.primrosemotorcycles.data.SparePartDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class JobCardStatusWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val jobCardDao: JobCardDao,
    private val sparePartDao: SparePartDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val jobCardId = inputData.getString("JOB_CARD_ID") ?: return Result.failure()

        val jobCard = jobCardDao.getById(jobCardId) ?: return Result.success()

        if (jobCard.getStatusEnum() == JobStatus.OPEN) {
            val parts = sparePartDao.getForJobSync(jobCardId)
            val sparesListText = jobCard.sparesList.trim()

            if (parts.isEmpty() && sparesListText.isEmpty()) {
                jobCardDao.updateStatus(jobCardId, JobStatus.WAITING_PARTS)

                try {
                    firestore.collection("branches")
                        .document(jobCard.branchId)
                        .collection("jobCards")
                        .document(jobCardId)
                        .update("status", JobStatus.WAITING_PARTS.name)
                } catch (_: Exception) {
                    // Local DB updated successfully; sync can happen later if offline
                }
            }
        }

        return Result.success()
    }
}
