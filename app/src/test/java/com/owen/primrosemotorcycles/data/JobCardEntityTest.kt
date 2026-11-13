package com.owen.primrosemotorcycles.data

import org.junit.Assert.assertEquals
import org.junit.Test

class JobCardEntityTest {

    @Test
    fun getStatusEnum_validStatuses_returnsCorrectEnum() {
        val cardOpen = JobCard(status = "OPEN")
        val cardInProgress = JobCard(status = "IN_PROGRESS")
        val cardWaiting = JobCard(status = "WAITING_PARTS")
        val cardDone = JobCard(status = "DONE")
        val cardCollected = JobCard(status = "COLLECTED")

        assertEquals(JobStatus.OPEN, cardOpen.getStatusEnum())
        assertEquals(JobStatus.IN_PROGRESS, cardInProgress.getStatusEnum())
        assertEquals(JobStatus.WAITING_PARTS, cardWaiting.getStatusEnum())
        assertEquals(JobStatus.DONE, cardDone.getStatusEnum())
        assertEquals(JobStatus.COLLECTED, cardCollected.getStatusEnum())
    }

    @Test
    fun getStatusEnum_invalidStatus_returnsDefaultOpen() {
        val cardInvalid = JobCard(status = "UNKNOWN_STATUS")
        assertEquals(JobStatus.OPEN, cardInvalid.getStatusEnum())
    }

    @Test
    fun quotationItem_calculatesExclPriceAndTotalExclCorrectly() {
        val item = QuotationItem(
            quotationId = "q1",
            description = "MOTUL 20W50",
            quantity = 2.0,
            inclPrice = 115.0
        )
        assertEquals(100.0, item.exclPrice, 0.001)
        assertEquals(200.0, item.totalExcl, 0.001)
    }

    @Test
    fun invoiceItem_calculatesExclPriceAndTotalExclCorrectly() {
        val item = InvoiceItem(
            invoiceId = "inv1",
            description = "REAR SHOCK",
            quantity = 3.0,
            inclPrice = 230.0
        )
        assertEquals(200.0, item.exclPrice, 0.001)
        assertEquals(600.0, item.totalExcl, 0.001)
    }

    @Test
    fun sparePart_calculatesTotalCorrectly() {
        val part = SparePart(
            jobCardId = "jc1",
            name = "Brake Pad",
            partNumber = "BP123",
            quantity = 2,
            unitPrice = 150.0
        )
        assertEquals(300.0, part.total, 0.001)
    }
}
