package com.example.mypiggybank.data

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    """
    SELECT 
        category,
        SUM(amount) as total,
        type
    FROM transactions 
    GROUP BY category, type
    """
)
data class CategorySummary(
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "total") val total: Double,
    @ColumnInfo(name = "type") val type: TransactionType
) 