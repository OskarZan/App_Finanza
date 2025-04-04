package com.example.financeszan.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeszan.data.FinancesDatabase
import com.example.financeszan.data.entities.Transaction
import com.example.financeszan.data.entities.TransactionCategory
import com.example.financeszan.data.entities.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class FinancesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FinancesDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _monthlyBalance = MutableStateFlow(0.0)
    val monthlyBalance: StateFlow<Double> = _monthlyBalance.asStateFlow()

    private val categoryTotals = mutableMapOf<TransactionCategory, MutableStateFlow<Double>>()

    init {
        loadTransactions()
        calculateMonthlyBalance()
        initCategoryTotals()
    }

    private fun initCategoryTotals() {
        TransactionCategory.values().forEach { category ->
            categoryTotals[category] = MutableStateFlow(0.0)
            updateCategoryTotal(category)
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            transactionDao.getAllTransactions().collect { transactions ->
                _transactions.value = transactions
                calculateMonthlyBalance()
                TransactionCategory.values().forEach { category ->
                    updateCategoryTotal(category)
                }
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
        }
    }
    
    fun deleteAllTransactions() {
        viewModelScope.launch {
            transactionDao.deleteAllTransactions()
        }
    }

    private fun calculateMonthlyBalance() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val income = transactionDao.getTotalAmountByTypeAndDateRange(
                TransactionType.INCOME,
                startOfMonth,
                endOfMonth
            ) ?: 0.0

            val expenses = transactionDao.getTotalAmountByTypeAndDateRange(
                TransactionType.EXPENSE,
                startOfMonth,
                endOfMonth
            ) ?: 0.0

            _monthlyBalance.value = income - expenses
        }
    }

    private fun updateCategoryTotal(category: TransactionCategory) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val total = transactionDao.getTotalAmountByCategoryAndDateRange(
                category,
                TransactionType.EXPENSE,
                startOfMonth,
                endOfMonth
            ) ?: 0.0
            
            categoryTotals[category]?.value = total
        }
    }

    fun getCategoryTotal(category: TransactionCategory): StateFlow<Double> {
        return categoryTotals[category] ?: MutableStateFlow(0.0)
    }
} 