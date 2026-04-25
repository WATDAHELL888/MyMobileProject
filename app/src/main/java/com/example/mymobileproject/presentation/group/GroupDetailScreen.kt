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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(group?.name ?: "", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }

        // Tabs
        TabRow(selectedTabIndex = state.selectedTab, containerColor = DarkSurface) {
            listOf(R.string.group_expenses, R.string.group_balances, R.string.group_settlement).forEachIndexed { i, titleRes ->
                Tab(selected = state.selectedTab == i, onClick = { viewModel.selectTab(i) },
                    text = { Text(stringResource(titleRes), fontSize = 12.sp) })
            }
        }

        // Content
        when (state.selectedTab) {
            0 -> ExpensesTab(state, group?.memberNames ?: emptyMap())
            1 -> BalancesTab(state, group?.memberNames ?: emptyMap())
            2 -> SettlementTab(state)
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

@Composable
private fun ExpensesTab(state: GroupDetailState, memberNames: Map<String, String>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        // Category summary
        if (state.categorySummary.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.group_category_summary), style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        state.categorySummary.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${catEmoji(cat)} ${cat.name.lowercase()}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text(CurrencyUtils.formatBaht(amt), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
        // Expense list
        items(state.expenses) { expense ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(catEmoji(expense.category), fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(expense.note.ifEmpty { expense.category.name }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${stringResource(R.string.group_paid_by)} ${memberNames[expense.paidBy] ?: expense.paidBy}",
                            style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                    Text(CurrencyUtils.formatBaht(expense.amount), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

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
            item { Text("✅ All settled!", color = TextSecondary, modifier = Modifier.padding(16.dp)) }
        }
        items(state.settlements) { settlement ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(settlement.fromName, style = MaterialTheme.typography.bodyMedium, color = ExpenseRed, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowForward, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(settlement.toName, style = MaterialTheme.typography.bodyMedium, color = IncomeGreen, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Text(CurrencyUtils.formatBaht(settlement.amount), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun catEmoji(c: TransactionCategory) = when(c) {
    TransactionCategory.FOOD->"🍔"; TransactionCategory.TRANSPORT->"🚗"; TransactionCategory.SHOPPING->"🛍️"
    TransactionCategory.ENTERTAINMENT->"🎬"; TransactionCategory.BILLS->"📄"; TransactionCategory.HEALTH->"💊"
    TransactionCategory.EDUCATION->"📚"; else->"📦"
}
