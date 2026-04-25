package com.example.mymobileproject.domain.usecase.transaction

import com.example.mymobileproject.domain.model.SpendingSummary
import com.example.mymobileproject.domain.model.Transaction
import com.example.mymobileproject.domain.model.TransactionType
import com.example.mymobileproject.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetTransactionSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
        endDate: LocalDate = LocalDate.now()
    ): Flow<SpendingSummary> {
        return repository.getTransactions(startDate, endDate).map { transactions ->
            calculateSummary(transactions, startDate, endDate)
        }
    }

    private fun calculateSummary(
        transactions: List<Transaction>,
        startDate: LocalDate,
        endDate: LocalDate
    ): SpendingSummary {
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val expense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val categoryBreakdown = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val dailySpending = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.date.dayOfMonth }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        return SpendingSummary(
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            categoryBreakdown = categoryBreakdown,
            dailySpending = dailySpending
        )
    }
}

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<String> {
        require(transaction.amount > 0) { "Amount must be positive" }
        return repository.addTransaction(transaction)
    }
}

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String): Result<Unit> {
        return repository.deleteTransaction(transactionId)
    }
}
