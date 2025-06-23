package com.example.mypiggybank.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mypiggybank.R
import com.example.mypiggybank.data.Budget
import com.example.mypiggybank.data.BudgetPeriod
import com.example.mypiggybank.repository.BudgetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class BudgetAlertWorker(
    @ApplicationContext private val context: Context,
    params: WorkerParameters,
    private val repository: BudgetRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "budget_alerts"
        const val CHANNEL_NAME = "Budget Alerts"
        const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis

        try {
            val budget = repository.getCurrentBudget().first()
            
            if (budget != null) {
                when (budget.period) {
                    BudgetPeriod.WEEKLY -> {
                        val endOfWeek = calendar.clone() as Calendar
                        endOfWeek.add(Calendar.DAY_OF_WEEK, 7)
                        
                        if (endOfWeek.timeInMillis - currentTime <= 24 * 60 * 60 * 1000) { // 24 hours before end
                            showNotification("Weekly Budget Ending", 
                                "Your weekly budget of $${String.format("%.2f", budget.amount)} is ending soon")
                        }
                    }
                    BudgetPeriod.MONTHLY -> {
                        val endOfMonth = calendar.clone() as Calendar
                        endOfMonth.set(Calendar.DAY_OF_MONTH, 
                            endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
                        
                        if (endOfMonth.timeInMillis - currentTime <= 2 * 24 * 60 * 60 * 1000) { // 2 days before end
                            showNotification("Monthly Budget Ending",
                                "Your monthly budget of $${String.format("%.2f", budget.amount)} is ending soon")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return Result.failure()
        }

        return Result.success()
    }

    private fun showNotification(title: String, content: String) {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Channel for budget alert notifications"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
} 