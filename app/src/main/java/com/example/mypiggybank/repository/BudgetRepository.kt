package com.example.mypiggybank.repository

import android.util.Log
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.BudgetPeriod
import com.example.mypiggybank.data.dao.BudgetDao
import com.example.mypiggybank.data.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BudgetRepository"

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun getCurrentBudget(): Flow<Budget?> {
        return budgetDao.getCurrentBudget()
            .map { entity -> entity?.toBudget() }
    }

    fun getBudgetForDateRange(startDate: Date, endDate: Date): Flow<Budget?> {
        return budgetDao.getBudgetForDateRange(startDate, endDate)
            .map { entity -> entity?.toBudget() }
    }

    suspend fun setNewBudget(budget: Budget) {
        try {
            Log.d(TAG, "Setting new budget: $budget")
            budgetDao.setNewBudget(BudgetEntity.fromBudget(budget))
            Log.d(TAG, "Budget set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting budget", e)
            throw e
        }
    }

    suspend fun insertBudget(budget: Budget) {
        try {
            budgetDao.insertBudget(BudgetEntity.fromBudget(budget))
            Log.d(TAG, "Successfully inserted budget")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting budget", e)
            throw e
        }
    }

    suspend fun updateBudget(budget: Budget) {
        try {
            budgetDao.updateBudget(BudgetEntity.fromBudget(budget))
            Log.d(TAG, "Successfully updated budget")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating budget", e)
            throw e
        }
    }

    suspend fun updateSpent(amount: Double) {
        try {
            budgetDao.updateSpent(amount)
            Log.d(TAG, "Successfully updated spent amount")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating spent amount", e)
            throw e
        }
    }

    suspend fun deleteBudget(budget: Budget) {
        try {
            budgetDao.deleteBudget(BudgetEntity.fromBudget(budget))
            Log.d(TAG, "Successfully deleted budget")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting budget", e)
            throw e
        }
    }

    suspend fun deleteAllBudgets() {
        try {
            budgetDao.deleteAllBudgets()
            Log.d(TAG, "Successfully deleted all budgets")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all budgets", e)
            throw e
        }
    }

    suspend fun getBudgetById(id: Long): Budget? {
        return try {
            budgetDao.getBudgetById(id)?.toBudget()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting budget by id", e)
            null
        }
    }

    suspend fun updateBudgetSpent(budgetId: Long, spent: Double) {
        try {
            budgetDao.updateBudgetSpent(budgetId, spent)
            Log.d(TAG, "Successfully updated budget spent amount")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating budget spent amount", e)
            throw e
        }
    }
} 