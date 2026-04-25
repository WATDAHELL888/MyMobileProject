package com.example.mymobileproject.domain.model

import java.time.LocalDateTime

data class Group(
    val id: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),        // UIDs
    val memberNames: Map<String, String> = emptyMap(), // uid -> displayName
    val createdBy: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class GroupExpense(
    val id: String = "",
    val groupId: String = "",
    val amount: Double = 0.0,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val note: String = "",
    val paidBy: String = "",         // UID of who paid
    val splitType: SplitType = SplitType.EQUAL,
    val splits: Map<String, Double> = emptyMap(),  // uid -> amount owed
    val date: java.time.LocalDate = java.time.LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class SplitType { EQUAL, CUSTOM, PERCENTAGE }

data class Settlement(
    val from: String,       // UID
    val fromName: String,
    val to: String,         // UID
    val toName: String,
    val amount: Double
)

data class GroupSummary(
    val totalExpenses: Double = 0.0,
    val expenseCount: Int = 0,
    val topSpender: String = "",
    val topSpenderAmount: Double = 0.0,
    val categoryBreakdown: Map<TransactionCategory, Double> = emptyMap(),
    val memberSpending: Map<String, Double> = emptyMap()  // uid -> total spent
)
