package com.example.mymobileproject.presentation.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mymobileproject.R
import com.example.mymobileproject.core.util.CurrencyUtils
import com.example.mymobileproject.domain.model.SplitType
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.ui.theme.*

@Composable
fun AddGroupExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddGroupExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onNavigateBack() }
    val group = state.group

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(stringResource(R.string.group_add_expense), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(16.dp))

        // Amount
        OutlinedTextField(value = state.amount, onValueChange = { viewModel.updateAmount(it) },
            modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.transaction_amount)) },
            prefix = { Text("฿ ", fontWeight = FontWeight.Bold, color = Emerald400) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp), singleLine = true)
        Spacer(Modifier.height(12.dp))

        // Note
        OutlinedTextField(value = state.note, onValueChange = { viewModel.updateNote(it) },
            modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.transaction_note)) },
            shape = RoundedCornerShape(12.dp), singleLine = true)
        Spacer(Modifier.height(16.dp))

        // Category selection
        Text(stringResource(R.string.transaction_category), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        val categories = listOf(
            TransactionCategory.FOOD to "🍔",
            TransactionCategory.TRANSPORT to "🚗",
            TransactionCategory.SHOPPING to "🛍️",
            TransactionCategory.ENTERTAINMENT to "🎬",
            TransactionCategory.BILLS to "📄",
            TransactionCategory.HEALTH to "💊",
            TransactionCategory.EDUCATION to "📚",
            TransactionCategory.OTHER to "📦"
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { (cat, emoji) ->
                val selected = state.category == cat
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { viewModel.updateCategory(cat) }
                        .then(if (selected) Modifier.border(2.dp, Emerald400, RoundedCornerShape(10.dp)) else Modifier),
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) Emerald500.copy(alpha = 0.15f) else DarkCard
                ) {
                    Text("$emoji ${cat.name.lowercase()}", Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = if (selected) Emerald400 else TextSecondary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Paid By — "ใครจ่าย?"
        Text(stringResource(R.string.group_paid_by) + " — ใครจ่าย?",
            style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(group?.members ?: emptyList()) { uid ->
                val name = group?.memberNames?.get(uid) ?: uid
                val selected = state.paidBy == uid
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { viewModel.updatePaidBy(uid) }
                        .then(if (selected) Modifier.border(2.dp, Blue400, RoundedCornerShape(10.dp)) else Modifier),
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) Blue500.copy(alpha = 0.15f) else DarkCard
                ) { Text("👤 $name", Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = if (selected) Blue400 else TextSecondary) }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Split With — "จ่ายให้ใคร?" (NEW!)
        Text(stringResource(R.string.group_split_with) + " — จ่ายให้ใคร?",
            style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(4.dp))
        Text("เลือกคนที่ต้องหาร (ไม่ต้องเลือกทุกคนก็ได้)", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        Spacer(Modifier.height(8.dp))

        // Select all / none toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            val allSelected = state.splitWithMembers.size == (group?.members?.size ?: 0)
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable {
                    if (allSelected) {
                        // Deselect all except payer
                        group?.members?.forEach { uid ->
                            if (uid != state.paidBy && uid in state.splitWithMembers) viewModel.toggleSplitMember(uid)
                        }
                    } else {
                        group?.members?.forEach { uid ->
                            if (uid !in state.splitWithMembers) viewModel.toggleSplitMember(uid)
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = if (allSelected) Emerald500.copy(alpha = 0.1f) else DarkCard
            ) {
                Text(
                    if (allSelected) "✓ ทั้งหมด" else stringResource(R.string.group_select_all),
                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (allSelected) Emerald400 else TextTertiary
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Member chips
        group?.members?.forEach { uid ->
            val name = group.memberNames[uid] ?: uid
            val selected = uid in state.splitWithMembers
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    .clickable { viewModel.toggleSplitMember(uid) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Blue500.copy(alpha = 0.1f) else DarkCard
                )
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = { viewModel.toggleSplitMember(uid) },
                        colors = CheckboxDefaults.colors(checkedColor = Blue400, uncheckedColor = TextTertiary),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("👤", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(name, style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else TextTertiary)
                    if (selected && uid == state.paidBy) {
                        Spacer(Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(6.dp), color = Emerald500.copy(alpha = 0.15f)) {
                            Text("ผู้จ่าย", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = Emerald400)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Split type
        Text("Split Type — วิธีหาร", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val splitLabels = listOf(
                SplitType.EQUAL to stringResource(R.string.group_split_equal),
                SplitType.CUSTOM to stringResource(R.string.group_split_custom),
                SplitType.PERCENTAGE to stringResource(R.string.group_split_percentage)
            )
            for ((type, label) in splitLabels) {
                val sel = state.splitType == type
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { viewModel.updateSplitType(type) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (sel) Emerald500.copy(alpha = 0.15f) else DarkCard
                ) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = if (sel) Emerald400 else TextTertiary, style = MaterialTheme.typography.labelLarge) }
            }
        }

        // Equal split preview
        if (state.splitType == SplitType.EQUAL && state.amount.toDoubleOrNull() != null && state.splitWithMembers.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            val share = (state.amount.toDouble()) / state.splitWithMembers.size
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.5f))) {
                Text("คนละ ${CurrencyUtils.formatBaht(share)} (${state.splitWithMembers.size} คน)",
                    Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        // Custom splits (only for CUSTOM / PERCENTAGE)
        if (state.splitType != SplitType.EQUAL) {
            Spacer(Modifier.height(12.dp))
            val splitMembers = group?.members?.filter { it in state.splitWithMembers } ?: emptyList()
            for (uid in splitMembers) {
                val name = group?.memberNames?.get(uid) ?: uid
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("👤 $name", Modifier.weight(1f), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = state.customSplits[uid] ?: "",
                        onValueChange = { viewModel.updateCustomSplit(uid, it) },
                        modifier = Modifier.width(100.dp),
                        suffix = { Text(if (state.splitType == SplitType.PERCENTAGE) "%" else "฿", color = TextTertiary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp), singleLine = true
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Error
        state.error?.let {
            Text(it, color = ExpenseRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Save button
        Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue500),
            enabled = state.amount.isNotBlank() && state.splitWithMembers.isNotEmpty() && !state.isSaving) {
            if (state.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(stringResource(R.string.save), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}
