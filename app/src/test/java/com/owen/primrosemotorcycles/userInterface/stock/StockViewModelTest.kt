package com.owen.primrosemotorcycles.userInterface.stock

import com.owen.primrosemotorcycles.data.StockDao
import com.owen.primrosemotorcycles.data.StockItem
import com.owen.primrosemotorcycles.data.StockTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeStockDao
    private lateinit var viewModel: StockViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeStockDao()
        viewModel = StockViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addStockIn_updatesQuantityAndInsertsTransaction() = runTest(testDispatcher) {
        val item = StockItem(id = "1", name = "Test Oil", quantityOnHand = 5, minQuantity = 2)
        fakeDao.insert(item)

        viewModel.addStockIn("1", 10, "Restock")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = fakeDao.getById("1")
        assertEquals(15, updated?.quantityOnHand)
        assertEquals(1, fakeDao.transactions.size)
        assertEquals("IN", fakeDao.transactions[0].type)
        assertEquals(10, fakeDao.transactions[0].quantityChange)
        assertEquals(15, fakeDao.transactions[0].quantityAfter)
    }

    @Test
    fun deductForJobCard_preventsNegativeStock() = runTest(testDispatcher) {
        val item = StockItem(id = "2", name = "Brake Pad", quantityOnHand = 2, minQuantity = 1)
        fakeDao.insert(item)

        // Try to deduct more than available (5 > 2)
        viewModel.deductForJobCard("2", 5, "JC-001")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = fakeDao.getById("2")
        assertEquals(2, updated?.quantityOnHand) // Should remain unchanged
        assertEquals(0, fakeDao.transactions.size)
    }

    @Test
    fun deductForJobCard_validQuantity_deductsAndLogs() = runTest(testDispatcher) {
        val item = StockItem(id = "3", name = "Spark Plug", quantityOnHand = 10, minQuantity = 2)
        fakeDao.insert(item)

        viewModel.deductForJobCard("3", 3, "JC-002")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = fakeDao.getById("3")
        assertEquals(7, updated?.quantityOnHand)
        assertEquals(1, fakeDao.transactions.size)
        assertEquals("SALE", fakeDao.transactions[0].type)
        assertEquals(-3, fakeDao.transactions[0].quantityChange)
        assertEquals(7, fakeDao.transactions[0].quantityAfter)
    }
}

class FakeStockDao : StockDao {
    private val items = mutableMapOf<String, StockItem>()
    val transactions = mutableListOf<StockTransaction>()

    override fun getAllStock(): Flow<List<StockItem>> =
        MutableStateFlow(items.values.toList())

    override fun getLowStock(): Flow<List<StockItem>> =
        MutableStateFlow(items.values.filter { it.isLowStock })

    override fun searchStock(query: String): Flow<List<StockItem>> =
        MutableStateFlow(items.values.filter { it.name.contains(query, ignoreCase = true) })

    override suspend fun getById(id: String): StockItem? = items[id]

    override suspend fun insert(item: StockItem) {
        items[item.id] = item
    }

    override suspend fun updateQty(id: String, newQty: Int, time: Long) {
        items[id]?.let {
            items[id] = it.copy(quantityOnHand = newQty, lastUpdated = time)
        }
    }

    override suspend fun insertTransaction(tx: StockTransaction) {
        transactions.add(tx)
    }

    override fun getTransactions(itemId: String): Flow<List<StockTransaction>> =
        MutableStateFlow(transactions.filter { it.stockItemId == itemId })

    override fun getRecentTransactions(): Flow<List<StockTransaction>> =
        MutableStateFlow(transactions)
}
