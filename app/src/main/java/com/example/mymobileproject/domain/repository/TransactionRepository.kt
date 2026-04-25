package com.example.mymobileproject.domain.repository

import com.example.mymobileproject.domain.model.Transaction
import com.example.mymobileproject.domain.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun getTransactions(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Result<String>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
    fun getTransactionsByCategory(
        category: TransactionCategory,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>>
}
