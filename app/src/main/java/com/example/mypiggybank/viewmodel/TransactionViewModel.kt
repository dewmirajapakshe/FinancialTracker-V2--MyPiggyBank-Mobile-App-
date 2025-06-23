package com.example.mypiggybank.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val TAG = "TransactionViewModel"

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                repository.getAllTransactions().collect { transactionList ->
                    _transactions.value = transactionList
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading transactions", e)
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.insertTransaction(transaction)
                Log.d(TAG, "Transaction added successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding transaction", e)
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                Log.d(TAG, "Transaction updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating transaction", e)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                Log.d(TAG, "Transaction deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting transaction", e)
            }
        }
    }
} 