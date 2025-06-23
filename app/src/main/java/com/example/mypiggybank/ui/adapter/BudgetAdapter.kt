package com.example.mypiggybank.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.BudgetPeriod
import java.text.SimpleDateFormat
import java.util.*

class BudgetAdapter(
    private val onItemClick: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    private var budgets: List<Budget> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPeriod: TextView = itemView.findViewById(R.id.tvPeriod)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvStartDate: TextView = itemView.findViewById(R.id.tvStartDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(budgets[position])
                }
            }
        }

        fun bind(budget: Budget) {
            tvPeriod.text = when (budget.period) {
                BudgetPeriod.WEEKLY -> "Weekly Budget"
                BudgetPeriod.MONTHLY -> "Monthly Budget"
            }
            tvAmount.text = String.format("Amount: $%.2f", budget.amount)
            tvStartDate.text = "Start Date: ${dateFormat.format(budget.startDate)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position])
    }

    override fun getItemCount(): Int = budgets.size

    fun updateBudgets(newBudgets: List<Budget>) {
        budgets = newBudgets
        notifyDataSetChanged()
    }
} 