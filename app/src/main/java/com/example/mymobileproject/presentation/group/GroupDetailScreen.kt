package com.example.mymobileproject.presentation.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mymobileproject.R
import com.example.mymobileproject.core.util.CurrencyUtils
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.ui.theme.*

@Composable
fun GroupDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: (String) -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val group = state.group
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Navigate back after group deleted
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onNavigateBack()
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ลบกลุ่ม", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("คุณต้องการลบกลุ่ม \"${group?.name}\" และรายการทั้งหมดใช่หรือไม่?\n\nการกระทำนี้ไม่สามารถย้อนกลับได้", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.deleteGroup() }) {
                    Text("ลบ", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก", color = TextTertiary)
                }
            },
            containerColor = DarkCard
        )
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Column(Modifier.weight(1f)) {
                Text(group?.name ?: "", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                Text("${group?.members?.size ?: 0} members", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Filled.Delete, "Delete Group", tint = ExpenseRed.copy(alpha = 0.7f))
            }
        }

        // Tabs — 4 tabs now
        val tabTitles = listOf(R.string.group_expenses, R.string.group_balances, R.string.group_settlement, R.string.group_summary)
        ScrollableTabRow(selectedTabIndex = state.selectedTab, containerColor = DarkSurface, edgePadding = 0.dp) {
            tabTitles.forEachIndexed { i, titleRes ->
                Tab(selected = state.selectedTab == i, onClick = { viewModel.selectTab(i) },
                    text = { Text(stringResource(titleRes), fontSize = 12.sp) })
            }
        }

        // Content
        when (state.selectedTab) {
            0 -> ExpensesTab(state, group?.memberNames ?: emptyMap(), viewModel)
            1 -> BalancesTab(state, group?.memberNames ?: emptyMap())
            2 -> SettlementTab(state)
            3 -> SummaryTab(state, group?.memberNames ?: emptyMap(), viewModel)
        }

        Spacer(Modifier.weight(1f))

        // Add Expense Button
        Button(
            onClick = { group?.id?.let { onNavigateToAddExpense(it) } },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue500)
        ) { Text(stringResource(R.string.group_add_expense), fontWeight = FontWeight.SemiBold) }
    }
}

// ── Expenses Tab ──
@Composable
private fun ExpensesTab(state: GroupDetailState, memberNames: Map<String, String>, viewModel: GroupDetailViewModel) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Total card
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.1f))) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💰", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(stringResource(R.string.group_total_expenses), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        Text(CurrencyUtils.formatBaht(state.totalExpenses), style = MaterialTheme.typography.headlineSmall,
                            color = Emerald400, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("${state.expenses.size} items", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
            }
        }

        // Top spender card
        state.topSpender?.let { (name, amount) ->
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.group_top_spender), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text("$name — ${CurrencyUtils.formatBaht(amount)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Expense list with swipe-to-delete
        items(state.expenses) { expense ->
            val paidByName = memberNames[expense.paidBy] ?: expense.paidBy
            val splitWithNames = expense.splits.keys.mapNotNull { memberNames[it] ?: it }

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(catEmoji(expense.category), fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(expense.note.ifEmpty { expense.category.name }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${stringResource(R.string.group_paid_by)} $paidByName",
                            style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        if (splitWithNames.size < (state.group?.members?.size ?: 0)) {
                            Text("→ ${splitWithNames.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall, color = Blue400)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(CurrencyUtils.formatBaht(expense.amount), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { viewModel.deleteExpense(expense.id) }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Delete, stringResource(R.string.group_delete_expense), tint = TextTertiary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Balances Tab ──
@Composable
private fun BalancesTab(state: GroupDetailState, memberNames: Map<String, String>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.balances.entries.toList()) { (uid, balance) ->
            val name = memberNames[uid] ?: uid
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = (if (balance >= 0) IncomeGreen else ExpenseRed).copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text("👤", fontSize = 18.sp) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(if (balance >= 0) stringResource(R.string.group_is_owed) else stringResource(R.string.group_owes),
                            style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                    Text(CurrencyUtils.formatBaht(kotlin.math.abs(balance)),
                        color = if (balance >= 0) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// ── Settlement Tab ──
@Composable
private fun SettlementTab(state: GroupDetailState) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.1f))) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(stringResource(R.string.group_smart_settlement), style = MaterialTheme.typography.titleMedium, color = Emerald400)
                        Text(stringResource(R.string.group_smart_settlement_desc), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
        if (state.settlements.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Box(Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.group_all_settled), color = IncomeGreen, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        items(state.settlements) { settlement ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    // From
                    Surface(shape = RoundedCornerShape(8.dp), color = ExpenseRed.copy(alpha = 0.12f)) {
                        Text(settlement.fromName, Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium, color = ExpenseRed, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowForward, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    // To
                    Surface(shape = RoundedCornerShape(8.dp), color = IncomeGreen.copy(alpha = 0.12f)) {
                        Text(settlement.toName, Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium, color = IncomeGreen, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(CurrencyUtils.formatBaht(settlement.amount), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Summary Tab (NEW) ──
@Composable
private fun SummaryTab(state: GroupDetailState, memberNames: Map<String, String>, viewModel: GroupDetailViewModel) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Overview card
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.1f))) {
                Column(Modifier.padding(16.dp)) {
                    Text("📊 " + stringResource(R.string.group_summary), style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryStatItem("💰", stringResource(R.string.group_total_expenses), CurrencyUtils.formatBaht(state.totalExpenses), Emerald400)
                        SummaryStatItem("📝", "Items", "${state.expenses.size}", Blue400)
                        SummaryStatItem("👥", "Members", "${state.group?.members?.size ?: 0}", Purple400)
                    }
                }
            }
        }

        // Member spending breakdown
        if (state.memberSpending.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("👤 " + stringResource(R.string.group_member_spending), style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        val maxSpend = state.memberSpending.values.maxOrNull() ?: 1.0
                        state.memberSpending.entries.sortedByDescending { it.value }.forEach { (uid, amount) ->
                            val name = memberNames[uid] ?: uid
                            val pct = (amount / state.totalExpenses * 100).toInt()
                            val barFraction = (amount / maxSpend).toFloat().coerceIn(0.05f, 1f)
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(name, Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Box(Modifier.weight(1f).height(20.dp).clip(RoundedCornerShape(4.dp)).background(DarkSurface)) {
                                    Box(Modifier.fillMaxHeight().fillMaxWidth(barFraction).clip(RoundedCornerShape(4.dp)).background(Blue400))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("${pct}%", Modifier.width(36.dp), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                Text(CurrencyUtils.formatBaht(amount), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Category breakdown
        if (state.categorySummary.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📂 " + stringResource(R.string.group_category_summary), style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        state.categorySummary.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                            val pct = if (state.totalExpenses > 0) (amt / state.totalExpenses * 100).toInt() else 0
                            val barFraction = if (state.totalExpenses > 0) (amt / state.totalExpenses).toFloat().coerceIn(0.03f, 1f) else 0f
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${catEmoji(cat)} ${catThaiName(cat)}", Modifier.width(100.dp),
                                    style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                                Box(Modifier.weight(1f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(DarkSurface)) {
                                    Box(Modifier.fillMaxHeight().fillMaxWidth(barFraction).clip(RoundedCornerShape(4.dp)).background(catColor(cat)))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("${pct}%", Modifier.width(32.dp), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                Text(CurrencyUtils.formatBaht(amt), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // AI Group Analysis
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖 AI วิเคราะห์กลุ่ม", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f))
                        Button(
                            onClick = { viewModel.analyzeWithAI() },
                            enabled = !state.isAnalyzing && state.expenses.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            if (state.isAnalyzing) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("กำลังวิเคราะห์...", style = MaterialTheme.typography.labelMedium)
                            } else {
                                Text("วิเคราะห์", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    if (state.aiAnalysis != null) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald500.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                state.aiAnalysis!!,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        }
                    } else if (state.expenses.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("เพิ่มรายการค่าใช้จ่ายก่อนเพื่อให้ AI วิเคราะห์", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }
        }

        // Add member section
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(16.dp)) {
                    Text("➕ " + stringResource(R.string.group_add_member), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.newMemberName,
                            onValueChange = { viewModel.updateNewMemberName(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Enter name") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.addMember() },
                            enabled = !state.isAddingMember
                        ) {
                            if (state.isAddingMember) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Filled.PersonAdd, "Add", tint = Emerald400)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatItem(emoji: String, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

private fun catEmoji(c: TransactionCategory) = when(c) {
    TransactionCategory.FOOD->"🍔"; TransactionCategory.TRANSPORT->"🚗"; TransactionCategory.SHOPPING->"🛍️"
    TransactionCategory.ENTERTAINMENT->"🎬"; TransactionCategory.BILLS->"📄"; TransactionCategory.HEALTH->"💊"
    TransactionCategory.EDUCATION->"📚"; else->"📦"
}

private fun catThaiName(c: TransactionCategory) = when(c) {
    TransactionCategory.FOOD->"อาหาร"; TransactionCategory.TRANSPORT->"เดินทาง"
    TransactionCategory.SHOPPING->"ช้อปปิ้ง"; TransactionCategory.ENTERTAINMENT->"บันเทิง"
    TransactionCategory.BILLS->"บิล"; TransactionCategory.HEALTH->"สุขภาพ"
    TransactionCategory.EDUCATION->"การศึกษา"; else->"อื่นๆ"
}

private fun catColor(c: TransactionCategory) = when(c) {
    TransactionCategory.FOOD -> Emerald400
    TransactionCategory.TRANSPORT -> Blue400
    TransactionCategory.SHOPPING -> Purple400
    TransactionCategory.ENTERTAINMENT -> Color(0xFFFF6B6B)
    TransactionCategory.BILLS -> Color(0xFFFFA726)
    TransactionCategory.HEALTH -> Color(0xFF66BB6A)
    TransactionCategory.EDUCATION -> Color(0xFF42A5F5)
    else -> TextTertiary
}
