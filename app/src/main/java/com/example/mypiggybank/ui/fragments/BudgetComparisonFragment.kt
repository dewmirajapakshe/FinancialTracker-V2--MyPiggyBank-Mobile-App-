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
import com.example.mypiggybank.R
import com.example.mypiggybank.data.TransactionType
import com.example.mypiggybank.databinding.FragmentBudgetComparisonBinding
import com.example.mypiggybank.viewmodel.DashboardViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "BudgetComparisonFragment"

@AndroidEntryPoint
class BudgetComparisonFragment : Fragment() {
    private var _binding: FragmentBudgetComparisonBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetComparisonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        observeBudgetData()
    }

    private fun setupChart() {
        binding.budgetComparisonChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            setDrawBorders(false)
            legend.isEnabled = true
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(false)
            animateY(1000, Easing.EaseInOutQuad)
        }
    }

    private fun observeBudgetData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentBudget.collectLatest { budgetAmount ->
                val spent = viewModel.getTotalByType(TransactionType.EXPENSE)
                updateChart(budgetAmount, spent)
            }
        }
    }

    private fun updateChart(budgetAmount: Double, spentAmount: Double) {
        if (budgetAmount == 0.0 && spentAmount == 0.0) {
            binding.budgetComparisonChart.clear()
            binding.budgetComparisonChart.setNoDataText("No budget data available")
            binding.budgetComparisonChart.invalidate()
            return
        }

        val entries = listOf(
            BarEntry(0f, budgetAmount.toFloat()),
            BarEntry(1f, spentAmount.toFloat())
        )

        val dataSet = BarDataSet(entries, "Budget vs Spent").apply {
            colors = listOf(
                Color.rgb(50, 205, 50),  // Green for budget
                if (spentAmount > budgetAmount) Color.RED else Color.BLUE  // Red if over budget, blue if under
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        binding.budgetComparisonChart.apply {
            data = BarData(dataSet)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(listOf("Budget", "Spent"))
            }
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 