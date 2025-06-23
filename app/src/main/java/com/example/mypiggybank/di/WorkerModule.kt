package com.example.mypiggybank.di

import android.content.Context
import androidx.work.WorkerParameters
import com.example.mypiggybank.notifications.BudgetAlertWorker
import com.example.mypiggybank.repository.BudgetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    fun provideBudgetAlertWorker(
        @ApplicationContext context: Context,
        params: WorkerParameters,
        repository: BudgetRepository
    ): BudgetAlertWorker {
        return BudgetAlertWorker(context, params, repository)
    }
} 