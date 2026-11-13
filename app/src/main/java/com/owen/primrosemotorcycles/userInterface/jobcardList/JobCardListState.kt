package com.owen.primrosemotorcycles.userInterface.jobcardList

import com.owen.primrosemotorcycles.data.JobStatus

// Events UI can send - no logic in UI
sealed interface JobListEvent {
    data class SearchChanged(val query: String) : JobListEvent
    data class StatusSelected(val status: JobStatus?) : JobListEvent
    object ClearFilters : JobListEvent
    object AddClicked : JobListEvent
    data class CardClicked(val id: String) : JobListEvent
    object StockClicked : JobListEvent
}
