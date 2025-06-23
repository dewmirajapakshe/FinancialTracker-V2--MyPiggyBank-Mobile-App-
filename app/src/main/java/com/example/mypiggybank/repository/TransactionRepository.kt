package com.example.mypiggybank.repository

import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.data.dao.TransactionDao
import com.example.mypiggybank.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type.name).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(TransactionEntity.fromTransaction(transaction))
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(TransactionEntity.fromTransaction(transaction))
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(TransactionEntity.fromTransaction(transaction))
    }

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun getTotalByType(type: TransactionType): Double {
        return transactionDao.getTotalByType(type) ?: 0.0
    }

    suspend fun getCategorySummary(type: TransactionType): Map<String, Double> {
        return transactionDao.getCategorySummaryList(type).associate { it.category to it.total }
    }

    suspend fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<Transaction> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate).map { it.toTransaction() }
    }
} 