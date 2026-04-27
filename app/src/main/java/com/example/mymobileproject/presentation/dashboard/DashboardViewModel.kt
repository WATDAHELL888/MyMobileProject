package com.example.mymobileproject.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymobileproject.domain.model.SpendingSummary
import com.example.mymobileproject.domain.model.Transaction
import com.example.mymobileproject.domain.repository.AIRepository
import com.example.mymobileproject.domain.repository.TransactionRepository
import com.example.mymobileproject.domain.usecase.transaction.GetTransactionSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import com.example.mymobileproject.widget.WidgetDataStore
import androidx.glance.appwidget.updateAll
import com.example.mymobileproject.widget.SmartFinanceWidget

data class SmartNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType
)

enum class NotificationType { DEBT, OVERSPEND, INSIGHT }

data class DashboardUiState(
    val summary: SpendingSummary = SpendingSummary(),
    val recentTransactions: List<Transaction> = emptyList(),
    val aiInsight: String = "",
    val isLoading: Boolean = true,
    val notifications: List<SmartNotification> = emptyList(),
    val showNotifications: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSummary: GetTransactionSummaryUseCase,
    private val transactionRepo: TransactionRepository,
    private val aiRepo: AIRepository,
    private val groupRepo: com.example.mymobileproject.domain.repository.GroupRepository,
    private val smartSettlement: com.example.mymobileproject.domain.usecase.group.SmartSettlementUseCase,
    private val authRepo: com.example.mymobileproject.domain.repository.AuthRepository
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
                
                // 1. UPDATE IMMEDIATELY: Save to Widget DataStore without waiting for AI
                viewModelScope.launch {
                    WidgetDataStore.saveWidgetData(
                        context = context,
                        balance = summary.balance,
                        spend = summary.totalExpense,
                        aiInsight = _uiState.value.aiInsight
                    )
                    SmartFinanceWidget().updateAll(context)
                }

                // 2. FETCH AI INSIGHT ASYNC: Don't block the flow collection
                viewModelScope.launch {
                    val result = aiRepo.getInsight(summary)
                    val newInsight = result.getOrNull() ?: _uiState.value.aiInsight
                    
                    _uiState.update { it.copy(aiInsight = newInsight) }
                    
                    // Update Widget again if insight changes
                    WidgetDataStore.saveWidgetData(
                        context = context,
                        balance = summary.balance,
                        spend = summary.totalExpense,
                        aiInsight = newInsight
                    )
                    SmartFinanceWidget().updateAll(context)
                }
            }
        }
        viewModelScope.launch {
            transactionRepo.getAllTransactions().collect { txns ->
                _uiState.update { it.copy(recentTransactions = txns.take(5)) }
                generateNotifications()
            }
        }
    }

    fun toggleNotifications() {
        _uiState.update { it.copy(showNotifications = !it.showNotifications) }
    }

    private fun generateNotifications() {
        viewModelScope.launch {
            val notifs = mutableListOf<SmartNotification>()
            val s = _uiState.value.summary

            // 1. Overspend warning
            if (s.totalExpense > 0 && s.balance < 0) {
                notifs.add(SmartNotification(
                    title = "⚠️ ระวังการใช้จ่าย",
                    message = "คุณใช้เงินเกินรายรับไปแล้ว ${com.example.mymobileproject.core.util.CurrencyUtils.formatBaht(-s.balance)} ในเดือนนี้",
                    type = NotificationType.OVERSPEND
                ))
            }

            // 2. Debt warning
            groupRepo.getUserGroups().collect { groups ->
                for (group in groups) {
                    val expenses = groupRepo.getGroupExpenses(group.id).first()
                    val settlements = smartSettlement(expenses, group.memberNames)
                    // We need to find settlements where the current user owes money.
                    // The current user's name is usually stored in AuthRepository
                    val currentUser = authRepo.currentUser.first()
                    val myName = currentUser?.displayName ?: "You"

                    // Only count debts where 'from' is me
                    val myDebts = settlements.filter { it.from.equals(myName, ignoreCase = true) || it.from.equals("You", ignoreCase = true) }
                    
                    if (myDebts.isNotEmpty()) {
                        val totalDebt = myDebts.sumOf { it.amount }
                        notifs.add(SmartNotification(
                            title = "💸 มีหนี้ค้างชำระ",
                            message = "คุณมียอดค้างชำระในกลุ่ม ${group.name} รวม ${com.example.mymobileproject.core.util.CurrencyUtils.formatBaht(totalDebt)}",
                            type = NotificationType.DEBT
                        ))
                    }
                }
                
                // Keep top 5
                _uiState.update { it.copy(notifications = notifs.distinctBy { n -> n.title }.take(5)) }
            }
        }
    }
}
