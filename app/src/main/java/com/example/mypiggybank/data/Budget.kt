package com.example.mypiggybank.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class BudgetPeriod {
    WEEKLY,
    MONTHLY
}

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Date,
    val spent: Double = 0.0,
    val alertThreshold: Double = 0.75 // Alert when 75% of budget is used
)