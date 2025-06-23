package com.example.mypiggybank.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.databinding.ActivityAddTransactionBinding
import com.example.mypiggybank.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import android.view.Window
import android.view.WindowManager
import android.graphics.Color

@AndroidEntryPoint
class AddTransactionActivity : AppCompatActivity() {
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var binding: ActivityAddTransactionBinding
    private var existingTransaction: Transaction? = null
    private var isEditMode = false

    private val categories = listOf(
        "Food", "Transport", "Shopping", "Bills", "Entertainment",
        "Health", "Education", "Salary", "Investment", "Other"
    )

    private val types = listOf("Income", "Expense")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Change status bar color to pink
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#FF69B4")

        // Check if we're in edit mode
        isEditMode = intent.getBooleanExtra("isEdit", false)
        if (isEditMode) {
            existingTransaction = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("transaction", Transaction::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("transaction")
            }
        }

        setupSpinners()
        if (isEditMode) {
            populateExistingData()
        }

        binding.btnSave.setOnClickListener {
            val amount = binding.etAmount.text.toString().toDoubleOrNull()
            val description = binding.etDescription.text.toString()
            val category = binding.spinnerCategory.text.toString()
            val type = when (binding.spinnerType.text.toString().lowercase()) {
                "income" -> TransactionType.INCOME
                else -> TransactionType.EXPENSE
            }

            if (amount != null && description.isNotEmpty() && category.isNotEmpty()) {
                val transaction = Transaction(
                    id = existingTransaction?.id ?: 0,
                    title = description,
                    amount = amount,
                    description = description,
                    category = category,
                    date = existingTransaction?.date ?: Date(),
                    type = type,
                    isIncome = type == TransactionType.INCOME,
                    notes = existingTransaction?.notes
                )

                if (isEditMode) {
                    viewModel.updateTransaction(transaction)
                } else {
                    viewModel.addTransaction(transaction)
                }
                setResult(RESULT_OK, intent.putExtra("transaction", transaction))
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners() {
        // Setup category spinner
        val categoryAdapter = ArrayAdapter(this, R.layout.item_dropdown, categories)
        binding.spinnerCategory.setAdapter(categoryAdapter)

        // Setup type spinner
        val typeAdapter = ArrayAdapter(this, R.layout.item_dropdown, types)
        binding.spinnerType.setAdapter(typeAdapter)
    }

    private fun populateExistingData() {
        existingTransaction?.let { transaction ->
            binding.apply {
                etDescription.setText(transaction.description)
                etAmount.setText(transaction.amount.toString())
                spinnerCategory.setText(transaction.category, false)
                spinnerType.setText(
                    if (transaction.type == TransactionType.INCOME) "Income" else "Expense",
                    false
                )
            }
        }
    }
} 