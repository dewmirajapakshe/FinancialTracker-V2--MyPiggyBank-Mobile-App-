package com.example.mypiggybank.data.dao

import androidx.room.*
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE strftime('%m', datetime(date/1000, 'unixepoch')) = :month AND strftime('%Y', datetime(date/1000, 'unixepoch')) = :year ORDER BY date DESC")
    fun getTransactionsForMonth(month: Int, year: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsBetweenDates(startDate: Date, endDate: Date): List<TransactionEntity>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    suspend fun getTotalByType(type: TransactionType): Double?

    @Query("""
        SELECT 
            category,
            SUM(amount) as total,
            type
        FROM transactions 
        WHERE type = :type 
        GROUP BY category, type
    """)
    suspend fun getCategorySummaryList(type: TransactionType): List<CategorySummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

data class CategorySummary(
    val category: String,
    val total: Double,
    val type: TransactionType
) 