package com.example.mymobileproject.presentation.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymobileproject.domain.model.*
import com.example.mymobileproject.domain.repository.GroupRepository
import com.example.mymobileproject.domain.usecase.group.SmartSettlementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Group List ──
data class GroupListState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = true,
    val isSeeding: Boolean = false,
    val seedMessage: String? = null
)

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val repo: GroupRepository,
    private val mockSeeder: com.example.mymobileproject.data.mock.GroupMockDataSeeder
) : ViewModel() {
    private val _state = MutableStateFlow(GroupListState())
    val state: StateFlow<GroupListState> = _state.asStateFlow()
    init {
        viewModelScope.launch {
            repo.getUserGroups().collect { groups ->
                _state.value = _state.value.copy(groups = groups, isLoading = false)
            }
        }
    }

    fun seedMockData() {
        viewModelScope.launch {
            _state.update { it.copy(isSeeding = true, seedMessage = null) }
            mockSeeder.seed()
                .onSuccess { _state.update { it.copy(isSeeding = false, seedMessage = "✅ Mock data created!") } }
                .onFailure { e -> _state.update { it.copy(isSeeding = false, seedMessage = "❌ ${e.message}") } }
        }
    }

    fun clearSeedMessage() { _state.update { it.copy(seedMessage = null) } }
}

// ── Create Group ──
data class CreateGroupState(
    val name: String = "",
    val memberNames: List<String> = emptyList(),
    val newMember: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val repo: GroupRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CreateGroupState())
    val state: StateFlow<CreateGroupState> = _state.asStateFlow()

    fun updateName(v: String) { _state.update { it.copy(name = v) } }
    fun updateNewMember(v: String) { _state.update { it.copy(newMember = v) } }
    fun addMember() {
        val name = _state.value.newMember.trim()
        if (name.isNotEmpty()) {
            _state.update { it.copy(memberNames = it.memberNames + name, newMember = "") }
        }
    }
    fun removeMember(name: String) {
        _state.update { it.copy(memberNames = it.memberNames - name) }
    }
    fun save() {
        if (_state.value.name.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repo.createGroup(_state.value.name, _state.value.memberNames)
                .onSuccess { _state.update { it.copy(saved = true, isSaving = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isSaving = false) } }
        }
    }
}

// ── Group Detail ──
data class GroupDetailState(
    val group: Group? = null,
    val expenses: List<GroupExpense> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val balances: Map<String, Double> = emptyMap(),
    val categorySummary: Map<TransactionCategory, Double> = emptyMap(),
    val memberSpending: Map<String, Double> = emptyMap(),
    val topSpender: Pair<String, Double>? = null,
    val totalExpenses: Double = 0.0,
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val newMemberName: String = "",
    val isAddingMember: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: GroupRepository,
    private val smartSettlement: SmartSettlementUseCase
) : ViewModel() {
    private val groupId: String = savedStateHandle["groupId"] ?: ""
    private val _state = MutableStateFlow(GroupDetailState())
    val state: StateFlow<GroupDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getGroup(groupId).collect { group ->
                _state.update { it.copy(group = group) }
            }
        }
        viewModelScope.launch {
            repo.getGroupExpenses(groupId).collect { expenses ->
                val group = _state.value.group
                val memberNames = group?.memberNames ?: emptyMap()
                val settlements = smartSettlement(expenses, memberNames)
                val balances = calculateBalances(expenses, memberNames)
                val catSummary = expenses.groupBy { it.category }.mapValues { (_, v) -> v.sumOf { it.amount } }
                val memberSpending = expenses.groupBy { it.paidBy }.mapValues { (_, v) -> v.sumOf { it.amount } }
                val topSpender = memberSpending.maxByOrNull { it.value }?.let {
                    (memberNames[it.key] ?: it.key) to it.value
                }
                val totalExpenses = expenses.sumOf { it.amount }
                _state.update {
                    it.copy(
                        expenses = expenses, settlements = settlements, balances = balances,
                        categorySummary = catSummary, memberSpending = memberSpending,
                        topSpender = topSpender, totalExpenses = totalExpenses, isLoading = false
                    )
                }
            }
        }
    }

    fun selectTab(idx: Int) { _state.update { it.copy(selectedTab = idx) } }

    fun updateNewMemberName(v: String) { _state.update { it.copy(newMemberName = v) } }

    fun addMember() {
        val name = _state.value.newMemberName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isAddingMember = true) }
            repo.addMember(groupId, name)
                .onSuccess { _state.update { it.copy(newMemberName = "", isAddingMember = false) } }
                .onFailure { _state.update { it.copy(isAddingMember = false) } }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repo.deleteExpense(groupId, expenseId)
        }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            repo.deleteGroup(groupId)
                .onSuccess { _state.update { it.copy(isDeleted = true) } }
        }
    }

    private fun calculateBalances(expenses: List<GroupExpense>, names: Map<String, String>): Map<String, Double> {
        val bal = mutableMapOf<String, Double>()
        expenses.forEach { e ->
            bal[e.paidBy] = (bal[e.paidBy] ?: 0.0) + e.amount
            e.splits.forEach { (uid, share) -> bal[uid] = (bal[uid] ?: 0.0) - share }
        }
        return bal
    }
}

// ── Add Group Expense ──
data class AddGroupExpenseState(
    val amount: String = "",
    val note: String = "",
    val category: TransactionCategory = TransactionCategory.FOOD,
    val paidBy: String = "",
    val splitType: SplitType = SplitType.EQUAL,
    val customSplits: Map<String, String> = emptyMap(),
    val splitWithMembers: Set<String> = emptySet(),  // NEW: who to split with
    val group: Group? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddGroupExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: GroupRepository
) : ViewModel() {
    private val groupId: String = savedStateHandle["groupId"] ?: ""
    private val _state = MutableStateFlow(AddGroupExpenseState())
    val state: StateFlow<AddGroupExpenseState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getGroup(groupId).collect { group ->
                if (group != null) {
                    _state.update {
                        it.copy(
                            group = group,
                            paidBy = group.members.firstOrNull() ?: "",
                            splitWithMembers = group.members.toSet(),  // default: all members
                            customSplits = group.members.associateWith { "" }
                        )
                    }
                }
            }
        }
    }

    fun updateAmount(v: String) { _state.update { it.copy(amount = v) } }
    fun updateNote(v: String) { _state.update { it.copy(note = v) } }
    fun updateCategory(v: TransactionCategory) { _state.update { it.copy(category = v) } }
    fun updatePaidBy(v: String) { _state.update { it.copy(paidBy = v) } }
    fun updateSplitType(v: SplitType) { _state.update { it.copy(splitType = v) } }
    fun updateCustomSplit(uid: String, v: String) {
        _state.update { it.copy(customSplits = it.customSplits + (uid to v)) }
    }

    // Toggle member in/out of split
    fun toggleSplitMember(uid: String) {
        _state.update {
            val newSet = if (uid in it.splitWithMembers) it.splitWithMembers - uid else it.splitWithMembers + uid
            it.copy(splitWithMembers = newSet)
        }
    }

    fun save() {
        val s = _state.value
        val amt = s.amount.toDoubleOrNull() ?: return
        val group = s.group ?: return
        val splitMembers = s.splitWithMembers.toList()
        if (splitMembers.isEmpty()) return

        val splits: Map<String, Double> = when (s.splitType) {
            SplitType.EQUAL -> {
                val share = amt / splitMembers.size
                splitMembers.associateWith { share }
            }
            SplitType.CUSTOM -> s.customSplits
                .filterKeys { it in splitMembers }
                .mapValues { it.value.toDoubleOrNull() ?: 0.0 }
            SplitType.PERCENTAGE -> s.customSplits
                .filterKeys { it in splitMembers }
                .mapValues { (it.value.toDoubleOrNull() ?: 0.0) / 100.0 * amt }
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repo.addExpense(groupId, GroupExpense(
                groupId = groupId, amount = amt, category = s.category,
                note = s.note, paidBy = s.paidBy, splitType = s.splitType, splits = splits
            )).onSuccess { _state.update { it.copy(saved = true, isSaving = false) } }
              .onFailure { e -> _state.update { it.copy(error = e.message, isSaving = false) } }
        }
    }
}
