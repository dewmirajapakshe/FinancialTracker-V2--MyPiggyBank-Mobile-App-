package com.example.mypiggybank.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.BudgetPeriod
import java.util.Date

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Date,
    val spent: Double = 0.0,
    val alertThreshold: Double = 0.75
) {
    fun toBudget(): Budget {
        return Budget(
            id = id,
            amount = amount,
            period = period,
            startDate = startDate,
            spent = spent,
            alertThreshold = alertThreshold
        )
    }

    companion object {
        fun fromBudget(budget: Budget): BudgetEntity {
            return BudgetEntity(
                id = budget.id,
                amount = budget.amount,
                period = budget.period,
                startDate = budget.startDate,
                spent = budget.spent,
                alertThreshold = budget.alertThreshold
            )
        }
    }
} 