package com.owen.primrosemotorcycles.userInterface.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owen.primrosemotorcycles.data.StockDao
import com.owen.primrosemotorcycles.data.StockItem
import com.owen.primrosemotorcycles.data.StockTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockUiState(
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val selectedTab: Int = 0
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val stockDao: StockDao
) : ViewModel() {

    private val _stockUiState = MutableStateFlow(StockUiState())
    val stockUiState: StateFlow<StockUiState> = _stockUiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _stockUiState.update { it.copy(searchQuery = query) }
        updateSearch(query)
    }

    fun setShowAddDialog(show: Boolean) {
        _stockUiState.update { it.copy(showAddDialog = show) }
    }

    fun setSelectedTab(tab: Int) {
        _stockUiState.update { it.copy(selectedTab = tab) }
    }

    val allStock = stockDao.getAllStock().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val lowStock = stockDao.getLowStock().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchResults = _searchQuery.flatMapLatest { q ->
        if (q.isBlank()) stockDao.getAllStock() else stockDao.searchStock(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun updateSearch(q: String) { _searchQuery.value = q }

    fun addStockIn(itemId: String, qty: Int, reason: String) {
        viewModelScope.launch {
            val item = stockDao.getById(itemId) ?: return@launch
            val newQty = item.quantityOnHand + qty
            stockDao.updateQty(itemId, newQty)
            stockDao.insertTransaction(
                StockTransaction(
                    stockItemId = itemId,
                    type = "IN",
                    quantityChange = qty,
                    quantityAfter = newQty,
                    reason = reason
                )
            )
        }
    }

    fun deductForJobCard(itemId: String, qty: Int, jobCardId: String) {
        viewModelScope.launch {
            val item = stockDao.getById(itemId) ?: return@launch
            if (item.quantityOnHand < qty) return@launch // prevent negative
            val newQty = item.quantityOnHand - qty
            stockDao.updateQty(itemId, newQty)
            stockDao.insertTransaction(
                StockTransaction(stockItemId = itemId, type = "SALE", quantityChange = -qty, quantityAfter = newQty, reason = "Used on Job $jobCardId", jobCardId = jobCardId)
            )
        }
    }

    fun createItem(partNumber: String, name: String, qty: Int, minQty: Int, cost: Double, sell: Double) {
        viewModelScope.launch {
            val item = StockItem(
                partNumber = partNumber,
                name = name,
                quantityOnHand = qty,
                minQuantity = minQty,
                costPrice = cost,
                sellingPrice = sell
            )
            stockDao.insert(item)
            stockDao.insertTransaction(
                StockTransaction(stockItemId = item.id, type = "IN", quantityChange = qty, quantityAfter = qty, reason = "Initial Stock")
            )
        }
    }
}
