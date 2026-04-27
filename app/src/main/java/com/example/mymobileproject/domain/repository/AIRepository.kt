package com.example.mymobileproject.domain.repository

import com.example.mymobileproject.domain.model.AIMessage
import com.example.mymobileproject.domain.model.BudgetRecommendation
import com.example.mymobileproject.domain.model.SpendingSummary

interface AIRepository {
    suspend fun chat(
        userMessage: String,
        history: List<AIMessage>,
        spendingSummary: SpendingSummary?,
        recentTransactions: List<com.example.mymobileproject.domain.model.Transaction> = emptyList()
    ): Result<String>

    suspend fun analyzeSpending(summary: SpendingSummary): Result<String>
    suspend fun getInsight(summary: SpendingSummary): Result<String>
    suspend fun getBudgetRecommendation(summary: SpendingSummary): Result<BudgetRecommendation>
    suspend fun detectAnomalies(summary: SpendingSummary, lastMonthSummary: SpendingSummary?): Result<String>
    suspend fun analyzeGroupSpending(
        groupName: String,
        expenses: List<com.example.mymobileproject.domain.model.GroupExpense>,
        memberNames: Map<String, String>
    ): Result<String>
}
