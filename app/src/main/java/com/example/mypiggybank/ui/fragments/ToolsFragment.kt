package com.example.mypiggybank.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mypiggybank.R
import com.example.mypiggybank.ui.MainViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.tabs.TabLayout
import android.widget.Button
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ToolsFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    // Currency conversion rates (base: USD)
    private val currencies = mapOf(
        "USD (US Dollar)" to 1.0,
        "EUR (Euro)" to 0.85,
        "GBP (British Pound)" to 0.73,
        "JPY (Japanese Yen)" to 110.0,
        "INR (Indian Rupee)" to 83.0,
        "AUD (Australian Dollar)" to 1.35,
        "CAD (Canadian Dollar)" to 1.25,
        "CHF (Swiss Franc)" to 0.92,
        "CNY (Chinese Yuan)" to 6.45,
        "SGD (Singapore Dollar)" to 1.35
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tools, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners(view)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<MaterialCardView>(R.id.cardEmiCalculator).setOnClickListener {
            showEmiCalculator()
        }

        view.findViewById<MaterialCardView>(R.id.cardInvestmentCalculator).setOnClickListener {
            showInvestmentCalculator()
        }

        view.findViewById<MaterialCardView>(R.id.cardCurrencyConverter).setOnClickListener {
            showCurrencyConverter()
        }

        view.findViewById<MaterialCardView>(R.id.cardSplitBill).setOnClickListener {
            showSplitBillCalculator()
        }
    }

    private fun showEmiCalculator() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_emi_calculator, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.emi_calculator)
            .setView(dialogView)
            .setPositiveButton("Calculate", null)
            .setNegativeButton("Close", null)
            .create()

        dialog.setOnShowListener {
            val calculateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            calculateButton.setOnClickListener {
                val principalInput = dialogView.findViewById<TextInputEditText>(R.id.etLoanAmount)
                val rateInput = dialogView.findViewById<TextInputEditText>(R.id.etInterestRate)
                val tenureInput = dialogView.findViewById<TextInputEditText>(R.id.etLoanTenure)
                val resultText = dialogView.findViewById<TextView>(R.id.tvEmiResult)

                val principal = principalInput.text.toString().toDoubleOrNull()
                val rate = rateInput.text.toString().toDoubleOrNull()
                val tenure = tenureInput.text.toString().toIntOrNull()

                if (principal != null && rate != null && tenure != null) {
                    val monthlyRate = rate / (12 * 100)
                    val months = tenure * 12
                    val emi = principal * monthlyRate * (1 + monthlyRate).pow(months) /
                            ((1 + monthlyRate).pow(months) - 1)
                    val totalPayment = emi * months
                    val totalInterest = totalPayment - principal

                    val result = """
                        Monthly EMI: ${currencyFormat.format(emi)}
                        Total Interest: ${currencyFormat.format(totalInterest)}
                        Total Payment: ${currencyFormat.format(totalPayment)}
                    """.trimIndent()

                    resultText.text = result
                    resultText.visibility = View.VISIBLE
                }
            }
        }

        dialog.show()
    }

    private fun showInvestmentCalculator() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_investment_calculator, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.investment_calculator)
            .setView(dialogView)
            .setPositiveButton("Calculate", null)
            .setNegativeButton("Close", null)
            .create()

        // Initialize views
        val tabLayout = dialogView.findViewById<TabLayout>(R.id.tabLayout)
        val tilInvestmentAmount = dialogView.findViewById<TextInputLayout>(R.id.tilInvestmentAmount)
        val etInvestmentAmount = dialogView.findViewById<TextInputEditText>(R.id.etInvestmentAmount)
        val etReturnRate = dialogView.findViewById<TextInputEditText>(R.id.etReturnRate)
        val etTimePeriod = dialogView.findViewById<TextInputEditText>(R.id.etTimePeriod)
        val tvInvestmentResult = dialogView.findViewById<TextView>(R.id.tvInvestmentResult)

        // Set initial hint for investment amount
        tilInvestmentAmount.hint = "Monthly Investment Amount"

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> tilInvestmentAmount.hint = "Monthly Investment Amount"
                    1 -> tilInvestmentAmount.hint = "One-time Investment Amount"
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        dialog.setOnShowListener {
            val calculateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            calculateButton.setOnClickListener {
                val amount = etInvestmentAmount.text.toString().toDoubleOrNull()
                val rate = etReturnRate.text.toString().toDoubleOrNull()
                val years = etTimePeriod.text.toString().toIntOrNull()

                if (amount != null && rate != null && years != null) {
                    val isSIP = tabLayout.selectedTabPosition == 0
                    val result = if (isSIP) {
                        calculateSIPReturns(amount, rate, years)
                    } else {
                        calculateLumpSumReturns(amount, rate, years)
                    }

                    tvInvestmentResult.apply {
                        text = result
                        visibility = View.VISIBLE
                    }
                }
            }
        }

        dialog.show()
    }

    private fun calculateSIPReturns(monthlyInvestment: Double, ratePercent: Double, years: Int): String {
        val monthlyRate = ratePercent / (12 * 100)
        val months = years * 12
        
        // Calculate future value of SIP
        // FV = P × ((1 + r)^n - 1) / r
        // Where P is monthly investment, r is monthly rate, n is number of months
        val futureValue = monthlyInvestment * ((1 + monthlyRate).pow(months) - 1) / monthlyRate * (1 + monthlyRate)
        val totalInvestment = monthlyInvestment * months
        val totalReturns = futureValue - totalInvestment

        return """
            Total Investment: ${currencyFormat.format(totalInvestment)}
            Expected Returns: ${currencyFormat.format(totalReturns)}
            Future Value: ${currencyFormat.format(futureValue)}
            
            Monthly Investment: ${currencyFormat.format(monthlyInvestment)}
            Time Period: $years years
            Expected Return Rate: $ratePercent%
        """.trimIndent()
    }

    private fun calculateLumpSumReturns(principal: Double, ratePercent: Double, years: Int): String {
        // Calculate future value of lump sum
        // FV = P(1 + r)^t
        // Where P is principal, r is annual rate, t is time in years
        val futureValue = principal * (1 + ratePercent/100).pow(years)
        val totalReturns = futureValue - principal

        return """
            Initial Investment: ${currencyFormat.format(principal)}
            Expected Returns: ${currencyFormat.format(totalReturns)}
            Future Value: ${currencyFormat.format(futureValue)}
            
            Time Period: $years years
            Expected Return Rate: $ratePercent%
        """.trimIndent()
    }

    private fun showCurrencyConverter() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_currency_converter, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.currency_converter)
            .setView(dialogView)
            .setPositiveButton("Convert", null)
            .setNegativeButton("Close", null)
            .create()

        // Initialize views
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val spinnerFromCurrency = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerFromCurrency)
        val spinnerToCurrency = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerToCurrency)
        val tvExchangeRate = dialogView.findViewById<TextView>(R.id.tvExchangeRate)
        val tvConvertedAmount = dialogView.findViewById<TextView>(R.id.tvConvertedAmount)

        // Setup currency spinners
        val currencyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            currencies.keys.toList()
        )
        spinnerFromCurrency.setAdapter(currencyAdapter)
        spinnerToCurrency.setAdapter(currencyAdapter)

        // Set default selections
        spinnerFromCurrency.setText(currencies.keys.first { it.contains("USD") }, false)
        spinnerToCurrency.setText(currencies.keys.first { it.contains("INR") }, false)

        dialog.setOnShowListener {
            val convertButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            convertButton.setOnClickListener {
                val amount = etAmount.text.toString().toDoubleOrNull()
                val fromCurrency = spinnerFromCurrency.text.toString()
                val toCurrency = spinnerToCurrency.text.toString()

                if (amount != null && fromCurrency in currencies && toCurrency in currencies) {
                    val fromRate = currencies[fromCurrency] ?: 1.0
                    val toRate = currencies[toCurrency] ?: 1.0
                    
                    // Convert to USD first, then to target currency
                    val amountInUSD = amount / fromRate
                    val convertedAmount = amountInUSD * toRate

                    // Calculate and display exchange rate
                    val exchangeRate = toRate / fromRate
                    tvExchangeRate.apply {
                        text = String.format("1 %s = %.4f %s", 
                            fromCurrency.split(" ")[0], 
                            exchangeRate,
                            toCurrency.split(" ")[0])
                        visibility = View.VISIBLE
                    }

                    // Display converted amount
                    tvConvertedAmount.apply {
                        text = String.format("%.2f %s = %.2f %s",
                            amount,
                            fromCurrency.split(" ")[0],
                            convertedAmount,
                            toCurrency.split(" ")[0])
                        visibility = View.VISIBLE
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showSplitBillCalculator() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_split_bill, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.split_bill)
            .setView(dialogView)
            .setPositiveButton("Calculate", null)
            .setNegativeButton("Close", null)
            .create()

        // Initialize views
        val etBillAmount = dialogView.findViewById<TextInputEditText>(R.id.etBillAmount)
        val etNumPeople = dialogView.findViewById<TextInputEditText>(R.id.etNumPeople)
        val etTipPercentage = dialogView.findViewById<TextInputEditText>(R.id.etTipPercentage)
        val tvSplitResult = dialogView.findViewById<TextView>(R.id.tvSplitResult)

        dialog.setOnShowListener {
            val calculateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            calculateButton.setOnClickListener {
                val billAmount = etBillAmount.text.toString().toDoubleOrNull()
                val numPeople = etNumPeople.text.toString().toIntOrNull()
                val tipPercentage = etTipPercentage.text.toString().toDoubleOrNull() ?: 0.0

                if (billAmount != null && numPeople != null && numPeople > 0) {
                    val tipAmount = billAmount * (tipPercentage / 100)
                    val totalAmount = billAmount + tipAmount
                    val amountPerPerson = totalAmount / numPeople

                    val result = """
                        Bill Amount: ${currencyFormat.format(billAmount)}
                        Tip (${String.format("%.1f", tipPercentage)}%): ${currencyFormat.format(tipAmount)}
                        Total Amount: ${currencyFormat.format(totalAmount)}
                        
                        Amount per person: ${currencyFormat.format(amountPerPerson)}
                    """.trimIndent()

                    tvSplitResult.apply {
                        text = result
                        visibility = View.VISIBLE
                    }
                }
            }
        }

        dialog.show()
    }
} 