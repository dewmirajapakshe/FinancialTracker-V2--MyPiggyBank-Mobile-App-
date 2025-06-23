package com.example.mypiggybank.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val TAG = "BudgetViewModel"

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _budgetInsertResult = MutableStateFlow<Boolean>(false)
    val budgetInsertResult: StateFlow<Boolean> = _budgetInsertResult.asStateFlow()

    val currentBudget = repository.getCurrentBudget()

    fun setNewBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Setting new budget: $budget")
                repository.setNewBudget(budget)
                _budgetInsertResult.value = true
                Log.d(TAG, "Budget set successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting budget", e)
                _budgetInsertResult.value = false
            }
        }
    }

    fun updateSpent(amount: Double) {
        viewModelScope.launch {
            try {
                repository.updateSpent(amount)
                Log.d(TAG, "Successfully updated spent amount")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating spent amount", e)
            }
        }
    }

    fun getBudgetForDateRange(startDate: Date, endDate: Date) = repository.getBudgetForDateRange(startDate, endDate)

    fun updateBudgetSpent(budgetId: Long, spent: Double) {
        viewModelScope.launch {
            try {
                repository.updateBudgetSpent(budgetId, spent)
                Log.d(TAG, "Successfully updated budget spent amount")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating budget spent amount", e)
            }
        }
    }
} 