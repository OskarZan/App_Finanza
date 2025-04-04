package com.example.financeszan.data

import android.app.Application
import com.example.financeszan.data.dao.TransactionDao
import com.example.financeszan.data.entities.Transaction
import com.example.financeszan.data.entities.TransactionCategory
import com.example.financeszan.data.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {
    
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }
    
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
    
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
    
    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }
    
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }
    
    fun getTransactionsByCategory(category: TransactionCategory): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category)
    }
    
    suspend fun getTotalAmountByTypeAndDateRange(
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): Double? {
        return transactionDao.getTotalAmountByTypeAndDateRange(type, startDate, endDate)
    }
    
    suspend fun getTotalAmountByCategoryAndDateRange(
        category: TransactionCategory,
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): Double? {
        return transactionDao.getTotalAmountByCategoryAndDateRange(category, type, startDate, endDate)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: TransactionRepository? = null
        
        fun getInstance(application: Application? = null): TransactionRepository {
            return INSTANCE ?: synchronized(this) {
                if (INSTANCE != null) {
                    return INSTANCE!!
                }
                
                if (application == null) {
                    throw IllegalArgumentException("Application no puede ser nulo para inicializar el repositorio")
                }
                
                val database = FinancesDatabase.getDatabase(application)
                val dao = database.transactionDao()
                val instance = TransactionRepository(dao)
                INSTANCE = instance
                instance
            }
        }
    }
} 