package com.example.mypiggybank.data.dao

import androidx.room.*
import com.example.mypiggybank.data.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE startDate BETWEEN :startDate AND :endDate")
    fun getBudgetForDateRange(startDate: Date, endDate: Date): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    @Query("SELECT * FROM budgets ORDER BY startDate DESC LIMIT 1")
    fun getCurrentBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Query("UPDATE budgets SET spent = :spent WHERE id = :budgetId")
    suspend fun updateBudgetSpent(budgetId: Long, spent: Double)

    @Query("UPDATE budgets SET spent = :amount")
    suspend fun updateSpent(amount: Double)

    @Transaction
    suspend fun setNewBudget(budget: BudgetEntity) {
        deleteAllBudgets()
        insertBudget(budget)
    }
} 