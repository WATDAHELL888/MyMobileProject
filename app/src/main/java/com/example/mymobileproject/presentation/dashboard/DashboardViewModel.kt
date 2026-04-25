package com.example.mymobileproject.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymobileproject.domain.model.SpendingSummary
import com.example.mymobileproject.domain.model.Transaction
import com.example.mymobileproject.domain.repository.AIRepository
import com.example.mymobileproject.domain.repository.TransactionRepository
import com.example.mymobileproject.domain.usecase.transaction.GetTransactionSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val summary: SpendingSummary = SpendingSummary(),
    val recentTransactions: List<Transaction> = emptyList(),
    val aiInsight: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getSummary: GetTransactionSummaryUseCase,
    private val transactionRepo: TransactionRepository,
    private val aiRepo: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Get this month's summary
            val startOfMonth = LocalDate.now().withDayOfMonth(1)
            getSummary(startOfMonth, LocalDate.now()).collect { summary ->
                _uiState.update { it.copy(summary = summary, isLoading = false) }
                // Get AI insight
                aiRepo.getInsight(summary).onSuccess { insight ->
                    _uiState.update { it.copy(aiInsight = insight) }
                }
            }
        }
        viewModelScope.launch {
            transactionRepo.getAllTransactions().collect { txns ->
                _uiState.update { it.copy(recentTransactions = txns.take(5)) }
            }
        }
    }
}
