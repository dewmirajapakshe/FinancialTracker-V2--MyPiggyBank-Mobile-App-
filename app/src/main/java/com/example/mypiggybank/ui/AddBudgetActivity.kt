package com.example.mypiggybank.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.BudgetPeriod
import com.example.mypiggybank.util.NotificationHelper
import com.example.mypiggybank.viewmodel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AddBudgetActivity : AppCompatActivity() {
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var etAmount: EditText
    private lateinit var rgPeriod: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)

        notificationHelper = NotificationHelper(this)
        etAmount = findViewById(R.id.etAmount)
        rgPeriod = findViewById(R.id.rgPeriod)
        btnSave = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val period = when (rgPeriod.checkedRadioButtonId) {
                R.id.rbWeekly -> BudgetPeriod.WEEKLY
                R.id.rbMonthly -> BudgetPeriod.MONTHLY
                else -> null
            }

            if (amount != null && period != null) {
                val budget = Budget(
                    amount = amount,
                    period = period,
                    startDate = Date(),
                    spent = 0.0,
                    alertThreshold = 0.8
                )
                viewModel.setNewBudget(budget)
            } else {
                Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            viewModel.budgetInsertResult.collectLatest { success ->
                if (success) {
                    val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                    notificationHelper.showBudgetAddedNotification(amount)
                    Toast.makeText(this@AddBudgetActivity, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddBudgetActivity, "Failed to save budget", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 