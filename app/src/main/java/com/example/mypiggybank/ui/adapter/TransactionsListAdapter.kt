package com.example.mypiggybank.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsListAdapter(
    private var transactionList: List<Transaction>,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionsListAdapter.ViewHolder>() {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)

        init {
            btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(transactionList[position])
                }
            }

            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(transactionList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_with_actions, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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