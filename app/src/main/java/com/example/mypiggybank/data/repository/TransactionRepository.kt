package com.example.mypiggybank.data.repository

import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.data.dao.TransactionDao
import com.example.mypiggybank.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

private const val TAG = "TransactionRepository"

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getTransactionsForMonth(month: Int, year: Int): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForMonth(month, year)
            .map { entities -> entities.map { it.toTransaction() } }
            .catch { e ->
                Log.e(TAG, "Error getting transactions for month", e)
                emit(emptyList())
            }
    }

    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
            .map { entities -> entities.map { it.toTransaction() } }
            .catch { e ->
                Log.e(TAG, "Error getting transactions", e)
                emit(emptyList())
            }
            .map { transactions ->
                Log.d(TAG, "Retrieved ${transactions.size} transactions")
                transactions
            }
    }

    suspend fun getTransactions(): List<Transaction> {
        return try {
            transactionDao.getAllTransactions()
                .map { entities -> entities.map { it.toTransaction() } }
                .first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting transactions", e)
            emptyList()
        }
    }

    suspend fun insertTransaction(transaction: Transaction) {
        try {
            transactionDao.insertTransaction(TransactionEntity.fromTransaction(transaction))
            Log.d(TAG, "Successfully inserted transaction: ${transaction.description}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting transaction", e)
            throw e
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        try {
            transactionDao.updateTransaction(TransactionEntity.fromTransaction(transaction))
            Log.d(TAG, "Successfully updated transaction: ${transaction.description}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction", e)
            throw e
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        try {
            transactionDao.deleteTransaction(TransactionEntity.fromTransaction(transaction))
            Log.d(TAG, "Successfully deleted transaction: ${transaction.description}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting transaction", e)
            throw e
        }
    }

    suspend fun getTotalByType(type: TransactionType): Double {
        return try {
            transactionDao.getTotalByType(type) ?: 0.0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total by type", e)
            0.0
        }
    }

    suspend fun getCategorySummary(type: TransactionType): Map<String, Double> {
        return try {
            transactionDao.getCategorySummaryList(type).associate { it.category to it.total }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category summary", e)
            emptyMap()
        }
    }
} 