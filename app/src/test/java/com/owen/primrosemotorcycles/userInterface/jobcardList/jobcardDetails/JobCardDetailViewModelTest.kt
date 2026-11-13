package com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails

import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.data.SparePart
import com.owen.primrosemotorcycles.userInterface.jobcardList.FakeJobCardDao
import com.owen.primrosemotorcycles.userInterface.jobcardList.FakeSparePartDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobCardDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeJobCardDao
    private lateinit var fakeSpareDao: FakeSparePartDao
    private lateinit var viewModel: JobCardDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeJobCardDao()
        fakeSpareDao = FakeSparePartDao()
        viewModel = JobCardDetailViewModel(fakeDao, fakeSpareDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun load_initialState_isNull() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.jobCard.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.jobCard.value)
    }

    @Test
    fun load_existingId_loadsJobCardAndSpares() = runTest(testDispatcher) {
        val card = JobCard(id = "jc-99", customerName = "Sam Wilson", bikeReg = "REG 99")
        val spare = SparePart(id = "sp-1", jobCardId = "jc-99", name = "Chain", partNumber = "CH01", quantity = 1, unitPrice = 450.0)
        fakeDao.insert(card)
        fakeSpareDao.insert(spare)

        backgroundScope.launch { viewModel.jobCard.collect {} }
        backgroundScope.launch { viewModel.spareParts.collect {} }

        viewModel.load("jc-99")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.jobCard.value)
        assertEquals("Sam Wilson", viewModel.jobCard.value?.customerName)
        assertEquals(1, viewModel.spareParts.value.size)
        assertEquals("Chain", viewModel.spareParts.value[0].name)
    }

    @Test
    fun updateField_modifiesJobCardInDao() = runTest(testDispatcher) {
        val card = JobCard(id = "jc-88", customerName = "Old Name")
        fakeDao.insert(card)

        backgroundScope.launch { viewModel.jobCard.collect {} }
        viewModel.load("jc-88")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateField { it.copy(customerName = "New Updated Name") }
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedInDao = fakeDao.getById("jc-88")
        assertEquals("New Updated Name", updatedInDao?.customerName)
        assertEquals("New Updated Name", viewModel.jobCard.value?.customerName)
    }
}
