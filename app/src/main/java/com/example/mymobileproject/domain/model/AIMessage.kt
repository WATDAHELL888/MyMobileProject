package com.example.mymobileproject.domain.model

import java.time.LocalDateTime

data class AIMessage(
    val id: String = "",
    val role: AIRole = AIRole.USER,
    val content: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class AIRole { USER, ASSISTANT }

data class SpendingSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryBreakdown: Map<TransactionCategory, Double> = emptyMap(),
    val dailySpending: Map<Int, Double> = emptyMap(),  // dayOfMonth -> amount
    val comparisonWithLastMonth: Double = 0.0  // percentage change
)

data class BudgetRecommendation(
    val suggestedBudget: Double = 0.0,
    val categoryBudgets: Map<TransactionCategory, Double> = emptyMap(),
    val tips: List<String> = emptyList(),
    val savingsPotential: Double = 0.0
)
