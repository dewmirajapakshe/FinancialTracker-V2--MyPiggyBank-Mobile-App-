package com.example.mypiggybank.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Transaction
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.databinding.FragmentDashboardBinding
import com.example.mypiggybank.ui.adapter.TransactionAdapter
import com.example.mypiggybank.viewmodel.DashboardViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

private const val TAG = "DashboardFragment"

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupPieChart()
        setupObservers()
        
        // Force a refresh of data
        viewModel.refreshData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            transactionList = emptyList(),
            onItemClick = { transaction -> 
                Log.d(TAG, "Transaction clicked: ${transaction.description}")
            }
        )
        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactions ->
                binding.tvTransactionCount.text = transactions.size.toString()
                updateDashboard(transactions)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val income = viewModel.getTotalByType(TransactionType.INCOME)
            binding.tvTotalIncome.text = String.format("₹%.2f", income)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val expenses = viewModel.getTotalByType(TransactionType.EXPENSE)
            binding.tvTotalExpenses.text = String.format("₹%.2f", expenses)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val income = viewModel.getTotalByType(TransactionType.INCOME)
            val expenses = viewModel.getTotalByType(TransactionType.EXPENSE)
            val balance = income - expenses
            binding.tvBalance.text = String.format("₹%.2f", balance)
        }
    }

    private fun updateDashboard(transactions: List<Transaction>) {
        Log.d(TAG, "Updating dashboard with ${transactions.size} transactions")
        updateCharts(transactions)
        transactionAdapter.updateTransactions(transactions)
    }

    private fun updateCharts(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            binding.pieChart.setNoDataText("No transactions to display")
            binding.pieChart.invalidate()
            return
        }

        // Group transactions by type and category
        val expensesByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> 
                transactions.sumOf { it.amount }
            }

        val incomeByCategory = transactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> 
                transactions.sumOf { it.amount }
            }

        Log.d(TAG, "Expenses by category: $expensesByCategory")
        Log.d(TAG, "Income by category: $incomeByCategory")

        // Create pie chart entries
        val entries = mutableListOf<PieEntry>()
        
        // Add expense entries (without labels)
        expensesByCategory.forEach { (_, amount) ->
            entries.add(PieEntry(amount.toFloat()))
        }
        
        // Add income entries (without labels)
        incomeByCategory.forEach { (_, amount) ->
            entries.add(PieEntry(amount.toFloat()))
        }

        if (entries.isEmpty()) {
            binding.pieChart.setNoDataText("No transactions to display")
            binding.pieChart.invalidate()
            return
        }

        // Create color list - Red shades for expenses, Green shades for income
        val colors = mutableListOf<Int>()
        val expenseColors = listOf(
            Color.rgb(255, 99, 71),  // Tomato
            Color.rgb(220, 20, 60),  // Crimson
            Color.rgb(178, 34, 34),  // FireBrick
            Color.rgb(139, 0, 0),    // DarkRed
            Color.rgb(255, 69, 0)    // OrangeRed
        )
        val incomeColors = listOf(
            Color.rgb(50, 205, 50),  // LimeGreen
            Color.rgb(34, 139, 34),  // ForestGreen
            Color.rgb(0, 128, 0),    // Green
            Color.rgb(0, 100, 0),    // DarkGreen
            Color.rgb(144, 238, 144) // LightGreen
        )

        // Assign colors - first expenses (red), then income (green)
        repeat(expensesByCategory.size) {
            colors.add(expenseColors[it % expenseColors.size])
        }
        repeat(incomeByCategory.size) {
            colors.add(incomeColors[it % incomeColors.size])
        }

        // Create and configure the dataset
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 16f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(binding.pieChart)
            valueLinePart1Length = 0.2f
            valueLinePart2Length = 0.4f
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }

        // Create and set the data
        val pieData = PieData(dataSet).apply {
            setValueTextSize(14f)
            setValueTextColor(Color.BLACK)
        }

        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false  // Remove description
            legend.isEnabled = false       // Remove legend
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(0f)      // Hide entry labels
            setDrawEntryLabels(false)      // Disable entry labels
            centerText = "Transactions"
            setCenterTextSize(16f)
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            invalidate() // refresh
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 