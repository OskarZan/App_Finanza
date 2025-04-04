package com.example.financeszan.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeszan.data.TransactionRepository
import com.example.financeszan.data.entities.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.time.ZonedDateTime

class CalendarViewModel(private val repository: TransactionRepository? = null) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _transactionsForSelectedDate = MutableStateFlow<List<Transaction>>(emptyList())
    val transactionsForSelectedDate: StateFlow<List<Transaction>> = _transactionsForSelectedDate.asStateFlow()
    
    // Mapa para almacenar totales por día
    private val _dailyTransactionTotals = MutableStateFlow<Map<LocalDate, Double>>(emptyMap())
    val dailyTransactionTotals: StateFlow<Map<LocalDate, Double>> = _dailyTransactionTotals.asStateFlow()
    
    private val _monthTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    
    init {
        repository?.let { repo ->
            viewModelScope.launch {
                repo.getAllTransactions().collect { transactions ->
                    _monthTransactions.value = transactions
                    processDailyTotals(transactions)
                    updateTransactionsForSelectedDate()
                }
            }
        }
    }
    
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        updateTransactionsForSelectedDate()
    }
    
    private fun updateTransactionsForSelectedDate() {
        try {
            val startOfDay = Date.from(_selectedDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val endOfDay = Date.from(_selectedDate.value.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1))
            
            _transactionsForSelectedDate.value = _monthTransactions.value.filter { transaction ->
                transaction.date in startOfDay..endOfDay
            }
        } catch (e: Exception) {
            _transactionsForSelectedDate.value = emptyList()
        }
    }
    
    private fun processDailyTotals(transactions: List<Transaction>) {
        try {
            val dailyTotals = mutableMapOf<LocalDate, Double>()
            
            transactions.forEach { transaction ->
                val localDate = transaction.date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                val amount = if (transaction.type.name == "INCOME") transaction.amount else -transaction.amount
                dailyTotals[localDate] = (dailyTotals[localDate] ?: 0.0) + amount
            }
            
            _dailyTransactionTotals.value = dailyTotals
        } catch (e: Exception) {
            _dailyTransactionTotals.value = emptyMap()
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: CalendarViewModel? = null
        
        fun getInstance(application: Application? = null): CalendarViewModel {
            return INSTANCE ?: synchronized(this) {
                if (INSTANCE != null) {
                    return INSTANCE!!
                }
                
                val repo = if (application != null) {
                    try {
                        TransactionRepository.getInstance(application)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } else {
                    null
                }
                
                val instance = CalendarViewModel(repo)
                INSTANCE = instance
                instance
            }
        }
    }
} 