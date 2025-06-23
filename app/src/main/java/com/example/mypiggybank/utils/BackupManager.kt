package com.example.mypiggybank.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.asFlow
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.repository.BudgetRepository
import com.example.mypiggybank.repository.TransactionRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BackupManager"
private const val BACKUP_FILE = "transactions_backup.json"

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) {
    private val gson = Gson()
    private val backupDir = File(context.filesDir, "backups")

    init {
        backupDir.mkdirs()
    }

    suspend fun createBackup(): Boolean {
        return try {
            val transactions = transactionRepository.getAllTransactions().first()
            val currentBudget = budgetRepository.getCurrentBudget().first()
            val budgets = listOfNotNull(currentBudget)

            val backup = BackupData(transactions, budgets)
            val json = gson.toJson(backup)
            
            val backupFile = File(backupDir, "backup_${System.currentTimeMillis()}.json")
            backupFile.writeText(json)
            
            // Keep only last 5 backups
            backupDir.listFiles()
                ?.sortedBy { it.lastModified() }
                ?.dropLast(5)
                ?.forEach { it.delete() }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreFromLatestBackup(): Boolean {
        return try {
            val latestBackup = backupDir.listFiles()
                ?.maxByOrNull { it.lastModified() }
                ?: return false

            val json = latestBackup.readText()
            val type = object : TypeToken<BackupData>() {}.type
            val backupData = gson.fromJson<BackupData>(json, type)

            // Clear existing data and restore from backup
            backupData.transactions.forEach { transactionRepository.insertTransaction(it) }
            backupData.budgets.firstOrNull()?.let { budgetRepository.setNewBudget(it) }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAvailableBackups(): List<BackupInfo> {
        return backupDir.listFiles()
            ?.map { file ->
                BackupInfo(
                    fileName = file.name,
                    date = java.util.Date(file.lastModified())
                )
            }
            ?.sortedByDescending { it.date }
            ?: emptyList()
    }

    private data class BackupData(
        val transactions: List<Transaction>,
        val budgets: List<Budget>
    )

    data class BackupInfo(
        val fileName: String,
        val date: java.util.Date
    )

    fun backupTransactions(transactions: List<Transaction>) {
        try {
            val file = File(context.filesDir, BACKUP_FILE)
            FileWriter(file).use { writer ->
                gson.toJson(transactions, writer)
            }
            Log.d(TAG, "Backup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up transactions", e)
        }
    }

    fun restoreTransactions(): List<Transaction> {
        return try {
            val file = File(context.filesDir, BACKUP_FILE)
            if (!file.exists()) {
                Log.d(TAG, "No backup file found")
                return emptyList()
            }

            FileReader(file).use { reader ->
                val type = object : TypeToken<List<Transaction>>() {}.type
                gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring transactions", e)
            emptyList()
        }
    }
} 