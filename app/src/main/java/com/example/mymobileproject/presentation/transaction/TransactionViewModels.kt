package com.example.mymobileproject.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymobileproject.domain.model.Transaction
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.domain.model.TransactionType
import com.example.mymobileproject.domain.repository.TransactionRepository
import com.example.mymobileproject.domain.usecase.transaction.AddTransactionUseCase
import com.example.mymobileproject.domain.usecase.transaction.DeleteTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// ── Transaction List ──
data class TransactionListState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val repo: TransactionRepository,
    private val deleteUseCase: DeleteTransactionUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(TransactionListState())
    val state: StateFlow<TransactionListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAllTransactions().collect { txns ->
                _state.value = TransactionListState(transactions = txns, isLoading = false)
            }
        }
    }

    fun delete(id: String) = viewModelScope.launch { deleteUseCase(id) }
}

// ── Add Transaction ──
data class AddTransactionState(
    val amount: String = "",
    val note: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory = TransactionCategory.FOOD,
    val date: LocalDate = LocalDate.now(),
    val quickAddText: String = "",
    val isSaving: Boolean = false,
    val isScanning: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addUseCase: AddTransactionUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    fun updateAmount(v: String) { _state.update { it.copy(amount = v) } }
    fun updateNote(v: String) { _state.update { it.copy(note = v) } }
    fun updateType(v: TransactionType) { _state.update { it.copy(type = v) } }
    fun updateCategory(v: TransactionCategory) { _state.update { it.copy(category = v) } }
    fun updateDate(v: LocalDate) { _state.update { it.copy(date = v) } }

    fun quickAdd(text: String) {
        _state.update { it.copy(quickAddText = text) }
        val regex = Regex("""^(.+?)\s+(\d+(?:\.\d{1,2})?)$""")
        val match = regex.find(text.trim()) ?: return
        val note = match.groupValues[1]
        val amount = match.groupValues[2]
        val category = TransactionCategory.guessFromText(note)
        _state.update { it.copy(note = note, amount = amount, category = category) }
    }

    fun save() {
        val s = _state.value
        val amt = s.amount.toDoubleOrNull() ?: run {
            _state.update { it.copy(error = "Invalid amount") }; return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            addUseCase(Transaction(amount = amt, type = s.type, category = s.category, note = s.note, date = s.date))
                .onSuccess { _state.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    fun scanReceiptMock() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, error = null) }
            kotlinx.coroutines.delay(1500) // Simulate OCR processing
            _state.update {
                it.copy(
                    isScanning = false,
                    amount = "120",
                    note = "Starbucks Coffee",
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.FOOD
                )
            }
        }
    }
}
