package com.example.mypiggybank.ui

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionCategory
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.databinding.DialogTransactionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class TransactionDialog(
    private val context: Context,
    private val onSave: (Transaction) -> Unit
) {
    private val binding = DialogTransactionBinding.inflate(LayoutInflater.from(context))
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private var transactionType = TransactionType.EXPENSE
    private var existingTransaction: Transaction? = null

    init {
        setupViews()
    }

    private fun setupViews() {
        // Setup category dropdown
        val incomeCategories = TransactionCategory.values()
            .filter { it.name.endsWith("INCOME") || it.name in listOf("SALARY", "BUSINESS", "INVESTMENTS", "GIFTS") }
            .map { it.displayName }
        val expenseCategories = TransactionCategory.values()
            .filter { it.name.endsWith("EXPENSE") || it !in incomeCategories.map { cat -> TransactionCategory.valueOf(cat.uppercase().replace(" ", "_")) } }
            .map { it.displayName }

        var currentCategories = expenseCategories
        val categoryAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, currentCategories)
        binding.categoryInput.setAdapter(categoryAdapter)

        // Setup date picker
        binding.dateInput.setText(dateFormatter.format(calendar.time))
        binding.dateLayout.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.dateInput.setOnClickListener {
            showDatePicker()
        }

        // Setup transaction type toggle
        binding.typeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.incomeButton.id -> {
                        transactionType = TransactionType.INCOME
                        currentCategories = incomeCategories
                    }
                    binding.expenseButton.id -> {
                        transactionType = TransactionType.EXPENSE
                        currentCategories = expenseCategories
                    }
                }
                categoryAdapter.clear()
                categoryAdapter.addAll(currentCategories)
                binding.categoryInput.setText("", false)
            }
        }

        // Set default selection
        binding.expenseButton.isChecked = true
    }

    private fun showDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                binding.dateInput.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun show(transaction: Transaction? = null) {
        existingTransaction = transaction
        if (transaction != null) {
            binding.apply {
                titleInput.setText(transaction.title)
                amountInput.setText(transaction.amount.toString())
                categoryInput.setText(transaction.category)
                calendar.time = transaction.date
                dateInput.setText(dateFormatter.format(calendar.time))
                notesInput.setText(transaction.notes)

                when (transaction.type) {
                    TransactionType.INCOME -> incomeButton.isChecked = true
                    TransactionType.EXPENSE -> expenseButton.isChecked = true
                }
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(if (transaction == null) "Add Transaction" else "Edit Transaction")
            .setView(binding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val title = binding.titleInput.text.toString()
                val amount = binding.amountInput.text.toString().toDoubleOrNull() ?: 0.0
                val category = binding.categoryInput.text.toString()
                val notes = binding.notesInput.text.toString().takeIf { it.isNotBlank() }

                val newTransaction = Transaction(
                    id = existingTransaction?.id ?: 0,
                    title = title,
                    amount = amount,
                    description = title,  // Using title as description for now
                    category = category,
                    date = calendar.time,
                    type = transactionType,
                    isIncome = transactionType == TransactionType.INCOME,
                    notes = notes
                )

                onSave(newTransaction)
            }
            .show()
    }
} 