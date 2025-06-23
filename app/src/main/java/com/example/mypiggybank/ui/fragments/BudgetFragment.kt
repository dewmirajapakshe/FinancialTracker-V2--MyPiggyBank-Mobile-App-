package com.example.mypiggybank.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypiggybank.R
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.ui.AddBudgetActivity
import com.example.mypiggybank.ui.adapter.BudgetAdapter
import com.example.mypiggybank.viewmodel.BudgetViewModel
import com.example.mypiggybank.viewmodel.DashboardViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetFragment : Fragment() {
    private val budgetViewModel: BudgetViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var budgetAdapter: BudgetAdapter
    private lateinit var chart: BarChart
    private lateinit var tvTotalBudgetStatus: TextView
    private lateinit var totalBudgetProgress: LinearProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up chart
        chart = view.findViewById(R.id.budgetComparisonChart)
        setupChart()

        // Set up budget status and progress
        tvTotalBudgetStatus = view.findViewById(R.id.tvTotalBudgetStatus)
        totalBudgetProgress = view.findViewById(R.id.totalBudgetProgress)

        // Set up RecyclerView
        budgetAdapter = BudgetAdapter { budget ->
            // Handle budget item click
            val intent = Intent(requireContext(), AddBudgetActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<RecyclerView>(R.id.rvBudgets).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetAdapter
        }

        // Set up Add Budget button
        view.findViewById<MaterialButton>(R.id.btnAddBudget).setOnClickListener {
            val intent = Intent(requireContext(), AddBudgetActivity::class.java)
            startActivity(intent)
        }

        // Observe budget and expenses
        viewLifecycleOwner.lifecycleScope.launch {
            budgetViewModel.currentBudget.collectLatest { budget ->
                val expenses = dashboardViewModel.getTotalByType(TransactionType.EXPENSE)
                budget?.let { 
                    budgetAdapter.updateBudgets(listOf(it))
                    updateChart(it.amount, expenses)
                    updateBudgetStatus(it.amount, expenses)
                    updateProgressIndicator(it.amount, expenses)
                } ?: run {
                    // No budget set
                    updateChart(0.0, expenses)
                    updateBudgetStatus(0.0, expenses)
                    updateProgressIndicator(0.0, expenses)
                    budgetAdapter.updateBudgets(emptyList())
                }
            }
        }
    }

    private fun updateBudgetStatus(budget: Double, spent: Double) {
        tvTotalBudgetStatus.text = String.format("₹%.2f / ₹%.2f", spent, budget)
    }

    private fun updateProgressIndicator(budget: Double, spent: Double) {
        if (budget > 0) {
            val progress = ((spent / budget) * 100).toInt()
            totalBudgetProgress.progress = progress.coerceIn(0, 100)
            
            // Set color based on progress
            totalBudgetProgress.setIndicatorColor(
                when {
                    progress >= 100 -> Color.RED
                    progress >= 75 -> Color.rgb(255, 165, 0) // Orange
                    else -> Color.rgb(76, 175, 80) // Green
                }
            )
        } else {
            totalBudgetProgress.progress = 0
            totalBudgetProgress.setIndicatorColor(Color.rgb(76, 175, 80)) // Green
        }
    }

    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setNoDataText("No budget data available")
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(listOf("Budget", "Expenses"))
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            
            animateY(1000)
        }
    }

    private fun updateChart(budget: Double, expense: Double) {
        if (budget == 0.0 && expense == 0.0) {
            chart.clear()
            chart.invalidate()
            return
        }

        val entries = listOf(
            BarEntry(0f, budget.toFloat()),  // Budget
            BarEntry(1f, expense.toFloat())  // Expenses
        )

        val dataSet = BarDataSet(entries, "Budget vs Expenses").apply {
            colors = listOf(
                Color.rgb(76, 175, 80),  // Green for budget
                if (expense > budget) {
                    Color.rgb(244, 67, 54)  // Red for over-budget expenses
                } else {
                    Color.rgb(33, 150, 243)  // Blue for within-budget expenses
                }
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        val barData = BarData(dataSet)
        chart.data = barData
        chart.invalidate()
    }
} 