package com.example.mymobileproject.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymobileproject.domain.model.AIMessage
import com.example.mymobileproject.domain.model.AIRole
import com.example.mymobileproject.domain.model.SpendingSummary
import com.example.mymobileproject.domain.repository.AIRepository
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
    val summary: SpendingSummary? = null
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiRepo: AIRepository,
    private val getSummary: GetTransactionSummaryUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AIChatState())
    val state: StateFlow<AIChatState> = _state.asStateFlow()

    init {
        // Load spending summary for context
        viewModelScope.launch {
            getSummary(LocalDate.now().withDayOfMonth(1), LocalDate.now()).collect { summary ->
                _state.update { it.copy(summary = summary) }
            }
        }
        // Welcome message
        _state.update {
            it.copy(messages = listOf(AIMessage(id = "welcome", role = AIRole.ASSISTANT,
                content = "สวัสดีครับ! 👋 ผมเป็น AI ที่ปรึกษาการเงินของคุณ ถามอะไรเกี่ยวกับการเงินได้เลยครับ! 💰")))
        }
    }

    fun updateInput(v: String) { _state.update { it.copy(input = v) } }

    fun send() {
        val text = _state.value.input.trim()
        if (text.isEmpty()) return
        val userMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.USER, content = text, timestamp = LocalDateTime.now())
        _state.update { it.copy(messages = it.messages + userMsg, input = "", isTyping = true) }
        viewModelScope.launch {
            aiRepo.chat(text, _state.value.messages, _state.value.summary)
                .onSuccess { reply ->
                    val aiMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.ASSISTANT, content = reply, timestamp = LocalDateTime.now())
                    _state.update { it.copy(messages = it.messages + aiMsg, isTyping = false) }
                }
                .onFailure {
                    val errMsg = AIMessage(id = UUID.randomUUID().toString(), role = AIRole.ASSISTANT, content = "ขออภัย เกิดข้อผิดพลาด กรุณาลองอีกครั้ง")
                    _state.update { it.copy(messages = it.messages + errMsg, isTyping = false) }
                }
        }
    }

    fun sendSuggestion(text: String) {
        _state.update { it.copy(input = text) }
        send()
    }
}
