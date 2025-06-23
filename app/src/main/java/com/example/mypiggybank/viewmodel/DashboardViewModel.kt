package com.example.mypiggybank.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.repository.BudgetRepository
import com.example.mypiggybank.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

private const val TAG = "DashboardViewModel"

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _currentBudget = MutableStateFlow<Double>(0.0)
    val currentBudget: StateFlow<Double> = _currentBudget.asStateFlow()

    init {
        loadData()
    }

    fun refreshData() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load transactions
                transactionRepository.getAllTransactions()
                    .catch { e ->
                        Log.e(TAG, "Error loading transactions", e)
                    }
                    .collect { transactions ->
                        _transactions.value = transactions
                    }

                // Load current budget
                budgetRepository.getCurrentBudget()
                    .catch { e ->
                        Log.e(TAG, "Error loading current budget", e)
                    }
                    .collect { budget ->
                        _currentBudget.value = budget?.amount ?: 0.0
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
            }
        }
    }

    suspend fun getTotalByType(type: TransactionType): Double {
        return try {
            transactionRepository.getTotalByType(type)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total for type: $type", e)
            0.0
        }
    }

    suspend fun getCategorySummary(type: TransactionType): Map<String, Double> {
        return try {
            transactionRepository.getCategorySummary(type)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category summary for type: $type", e)
            emptyMap()
        }
    }

    suspend fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<Transaction> {
        return try {
            transactionRepository.getTransactionsByDateRange(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting transactions by date range", e)
            emptyList()
        }
    }
} 