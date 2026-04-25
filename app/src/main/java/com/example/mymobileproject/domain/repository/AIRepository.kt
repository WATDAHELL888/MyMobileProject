package com.example.mymobileproject.domain.repository

import com.example.mymobileproject.domain.model.AIMessage
import com.example.mymobileproject.domain.model.SpendingSummary

interface AIRepository {
    suspend fun chat(
        userMessage: String,
        history: List<AIMessage>,
        spendingSummary: SpendingSummary?
    ): Result<String>

    suspend fun analyzeSpending(summary: SpendingSummary): Result<String>
    suspend fun getInsight(summary: SpendingSummary): Result<String>
}
