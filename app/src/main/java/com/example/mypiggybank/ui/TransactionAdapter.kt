package com.example.mypiggybank.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val onItemLongClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencyFormatter = NumberFormat.getCurrencyInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition))
                }
            }

            binding.root.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(adapterPosition))
                    true
                } else false
            }
        }

        fun bind(transaction: Transaction) {
            binding.apply {
                titleText.text = transaction.description
                categoryText.text = transaction.category
                dateText.text = dateFormatter.format(transaction.date)

                val amount = currencyFormatter.format(transaction.amount)
                amountText.text = when (transaction.type) {
                    TransactionType.EXPENSE -> "-$amount"
                    TransactionType.INCOME -> "+$amount"
                }
                amountText.setTextColor(
                    root.context.getColor(
                        when (transaction.type) {
                            TransactionType.EXPENSE -> R.color.expense_red
                            TransactionType.INCOME -> R.color.income_green
                        }
                    )
                )

                // Set category icon based on category
                categoryIcon.setImageResource(getCategoryIcon(transaction.category))
            }
        }

        private fun getCategoryIcon(category: String): Int {
            return when (category.lowercase()) {
                "food & dining" -> R.drawable.ic_food
                "shopping" -> R.drawable.ic_shopping
                "transportation" -> R.drawable.ic_transport
                "bills & utilities" -> R.drawable.ic_bills
                "entertainment" -> R.drawable.ic_entertainment
                "health & medical" -> R.drawable.ic_health
                "education" -> R.drawable.ic_education
                "housing & rent" -> R.drawable.ic_home
                "travel" -> R.drawable.ic_travel
                "salary" -> R.drawable.ic_salary
                "business" -> R.drawable.ic_business
                "investments" -> R.drawable.ic_investment
                "gifts" -> R.drawable.ic_gift
                else -> R.drawable.ic_other
            }
        }
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
} 