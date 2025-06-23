package com.example.mypiggybank.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactionList: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(transactionList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.tvDescription.text = transaction.description
        holder.tvCategory.text = transaction.category
        holder.tvDate.text = dateFormatter.format(transaction.date)
        
        // Format amount with Rupee symbol and color
        val amountText = if (transaction.type == TransactionType.INCOME) 
            "+₹${transaction.amount}" else "-₹${transaction.amount}"
        holder.tvAmount.text = amountText
        
        // Set color based on transaction type
        holder.tvAmount.setTextColor(
            holder.itemView.context.getColor(
                if (transaction.type == TransactionType.INCOME) 
                    R.color.income_green 
                else 
                    R.color.expense_red
            )
        )
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactionList = newTransactions
        notifyDataSetChanged()
    }
}
