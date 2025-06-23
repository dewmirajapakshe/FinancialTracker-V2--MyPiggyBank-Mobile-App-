package com.example.mypiggybank.data.entity

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import java.util.Date

private const val TAG = "TransactionEntity"

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val description: String,
    val category: String,
    val date: Date,
    val type: TransactionType,
    val isIncome: Boolean,
    val notes: String? = null
) {
    fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            description = description,
            category = category,
            date = date,
            type = type,
            isIncome = isIncome,
            notes = notes
        )
    }

    companion object {
        fun fromTransaction(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                title = transaction.title,
                amount = transaction.amount,
                description = transaction.description,
                category = transaction.category,
                date = transaction.date,
                type = transaction.type,
                isIncome = transaction.isIncome,
                notes = transaction.notes
            )
        }
    }
} 