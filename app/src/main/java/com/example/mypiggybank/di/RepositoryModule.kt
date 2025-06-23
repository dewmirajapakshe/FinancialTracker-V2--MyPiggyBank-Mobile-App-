package com.example.mypiggybank.di

import com.example.mypiggybank.data.dao.BudgetDao
import com.example.mypiggybank.data.dao.TransactionDao
import com.example.mypiggybank.data.repository.TransactionRepository
import com.example.mypiggybank.repository.BudgetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao
    ): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: BudgetDao
    ): BudgetRepository {
        return BudgetRepository(budgetDao)
    }
}