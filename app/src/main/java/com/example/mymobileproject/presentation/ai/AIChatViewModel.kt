package com.example.mymobileproject.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymobileproject.domain.model.AIMessage
import com.example.mymobileproject.domain.model.AIRole
import com.example.mymobileproject.domain.model.SpendingSummary
import com.example.mymobileproject.domain.repository.AIRepository
import com.example.mymobileproject.domain.repository.TransactionRepository
import com.example.mymobileproject.domain.model.Transaction
import com.example.mymobileproject.domain.usecase.transaction.GetTransactionSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class AIChatState(
    val messages: List<AIMessage> = emptyList(),
    val input: String = "",
    val isTyping: Boolean = false,
    val summary: SpendingSummary? = null,
    val quickInsight: String? = null,
    val recentTransactions: List<Transaction> = emptyList()
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiRepo: AIRepository,
    private val getSummary: GetTransactionSummaryUseCase,
    private val transactionRepo: TransactionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AIChatState())
    val state: StateFlow<AIChatState> = _state.asStateFlow()

    init {
        // Load spending summary for context
        viewModelScope.launch {
            getSummary(LocalDate.now().withDayOfMonth(1), LocalDate.now()).collect { summary ->
                _state.update { it.copy(summary = summary) }
                // Generate quick insight for dashboard
                aiRepo.getInsight(summary).onSuccess { insight ->
                    _state.update { it.copy(quickInsight = insight) }
                }
            }
        }
        viewModelScope.launch {
            transactionRepo.getTransactions(LocalDate.now().withDayOfMonth(1), LocalDate.now()).collect { txns ->
                _state.update { it.copy(recentTransactions = txns) }
            }
        }
        // Welcome message with auto-analysis if data exists
        _state.update {
            it.copy(messages = listOf(AIMessage(
                id = "welcome", role = AIRole.ASSISTANT,
                content = "สวัสดีครับ! 👋 ผมเป็น AI ที่ปรึกษาการเงินของคุณ\n\nผมช่วยได้:\n🔍 วิเคราะห์พฤติกรรมการเงิน\n💡 แนะนำวิธีประหยัด\n📊 แนะนำ budget\n⚠️ ตรวจจับความผิดปกติ\n👥 วิเคราะห์กลุ่ม\n\nลองถามดูครับ! 💰"
            )))
        }
    }

    fun updateInput(v: String) { _state.update { it.copy(input = v) } }

    fun send() {
        val text = _state.value.input.trim()
        if (text.isEmpty()) return
        val userMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.USER, content = text, timestamp = LocalDateTime.now())
        _state.update { it.copy(messages = it.messages + userMsg, input = "", isTyping = true) }
        viewModelScope.launch {
            aiRepo.chat(text, _state.value.messages, _state.value.summary, _state.value.recentTransactions)
                .onSuccess { reply ->
                    val aiMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.ASSISTANT, content = reply, timestamp = LocalDateTime.now())
                    _state.update { it.copy(messages = it.messages + aiMsg, isTyping = false) }
                }
                .onFailure {
                    val errMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.ASSISTANT, content = "ขออภัย เกิดข้อผิดพลาด กรุณาลองอีกครั้ง 🙏")
                    _state.update { it.copy(messages = it.messages + errMsg, isTyping = false) }
                }
        }
    }

    fun sendSuggestion(text: String) {
        _state.update { it.copy(input = text) }
        send()
    }

    // Quick actions
    fun analyzeSpending() {
        val s = _state.value.summary ?: return
        _state.update { it.copy(input = "") }
        val userMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.USER, content = "วิเคราะห์การใช้จ่าย")
        _state.update { it.copy(messages = it.messages + userMsg, isTyping = true) }
        viewModelScope.launch {
            aiRepo.analyzeSpending(s)
                .onSuccess { reply ->
                    val aiMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.ASSISTANT, content = reply)
                    _state.update { it.copy(messages = it.messages + aiMsg, isTyping = false) }
                }
                .onFailure { _state.update { it.copy(isTyping = false) } }
        }
    }

    fun detectAnomalies() {
        val s = _state.value.summary ?: return
        val userMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.USER, content = "ตรวจจับความผิดปกติ")
        _state.update { it.copy(messages = it.messages + userMsg, isTyping = true) }
        viewModelScope.launch {
            aiRepo.detectAnomalies(s, null)
                .onSuccess { reply ->
                    val aiMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.ASSISTANT, content = reply)
                    _state.update { it.copy(messages = it.messages + aiMsg, isTyping = false) }
                }
                .onFailure { _state.update { it.copy(isTyping = false) } }
        }
    }

    fun recommendBudget() {
        val s = _state.value.summary ?: return
        val userMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.USER, content = "แนะนำ budget")
        _state.update { it.copy(messages = it.messages + userMsg, isTyping = true) }
        viewModelScope.launch {
            aiRepo.getBudgetRecommendation(s)
                .onSuccess { rec ->
                    val reply = buildString {
                        append("📊 Budget เดือนหน้า:\n\n")
                        append("💰 งบรวม: ${com.example.mymobileproject.core.util.CurrencyUtils.formatBaht(rec.suggestedBudget)}\n\n")
                        rec.categoryBudgets.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                            append("${catEmoji(cat)} ${catTh(cat)}: ${com.example.mymobileproject.core.util.CurrencyUtils.formatBaht(amt)}\n")
                        }
                        if (rec.tips.isNotEmpty()) {
                            append("\n💡 Tips:\n")
                            rec.tips.forEach { append("• $it\n") }
                        }
                        append("\n🎯 ศักยภาพเก็บเงิน: ${com.example.mymobileproject.core.util.CurrencyUtils.formatBaht(rec.savingsPotential)}/เดือน")
                    }
                    val aiMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.ASSISTANT, content = reply)
                    _state.update { it.copy(messages = it.messages + aiMsg, isTyping = false) }
                }
                .onFailure { _state.update { it.copy(isTyping = false) } }
        }
    }

    private fun catTh(c: com.example.mymobileproject.domain.model.TransactionCategory) = when(c) {
        com.example.mymobileproject.domain.model.TransactionCategory.FOOD->"อาหาร"
        com.example.mymobileproject.domain.model.TransactionCategory.TRANSPORT->"เดินทาง"
        com.example.mymobileproject.domain.model.TransactionCategory.SHOPPING->"ช้อปปิ้ง"
        com.example.mymobileproject.domain.model.TransactionCategory.ENTERTAINMENT->"บันเทิง"
        com.example.mymobileproject.domain.model.TransactionCategory.BILLS->"บิล"
        com.example.mymobileproject.domain.model.TransactionCategory.HEALTH->"สุขภาพ"
        com.example.mymobileproject.domain.model.TransactionCategory.EDUCATION->"การศึกษา"
        else->"อื่นๆ"
    }
    private fun catEmoji(c: com.example.mymobileproject.domain.model.TransactionCategory) = when(c) {
        com.example.mymobileproject.domain.model.TransactionCategory.FOOD->"🍔"
        com.example.mymobileproject.domain.model.TransactionCategory.TRANSPORT->"🚗"
        com.example.mymobileproject.domain.model.TransactionCategory.SHOPPING->"🛍️"
        com.example.mymobileproject.domain.model.TransactionCategory.ENTERTAINMENT->"🎬"
        com.example.mymobileproject.domain.model.TransactionCategory.BILLS->"📄"
        com.example.mymobileproject.domain.model.TransactionCategory.HEALTH->"💊"
        com.example.mymobileproject.domain.model.TransactionCategory.EDUCATION->"📚"
        else->"📦"
    }
}
