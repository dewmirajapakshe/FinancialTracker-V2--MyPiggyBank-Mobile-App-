package com.example.mypiggybank.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        Date(parcel.readLong()),
        TransactionType.valueOf(parcel.readString() ?: TransactionType.EXPENSE.name),
        parcel.readInt() == 1,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeDouble(amount)
        parcel.writeString(description)
        parcel.writeString(category)
        parcel.writeLong(date.time)
        parcel.writeString(type.name)
        parcel.writeInt(if (isIncome) 1 else 0)
        parcel.writeString(notes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}

enum class TransactionType {
    INCOME, EXPENSE
}

enum class TransactionCategory(val displayName: String) {
    // Income Categories
    SALARY("Salary"),
    BUSINESS("Business"),
    INVESTMENTS("Investments"),
    GIFTS("Gifts"),
    OTHER_INCOME("Other Income"),

    // Expense Categories
    FOOD("Food & Dining"),
    SHOPPING("Shopping"),
    TRANSPORTATION("Transportation"),
    BILLS("Bills & Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health & Medical"),
    EDUCATION("Education"),
    HOUSING("Housing & Rent"),
    TRAVEL("Travel"),
    SAVINGS("Savings"),
    OTHER_EXPENSE("Other Expenses")
}