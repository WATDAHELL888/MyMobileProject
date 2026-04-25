package com.example.mymobileproject.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mymobileproject.R
import com.example.mymobileproject.core.util.CurrencyUtils
import com.example.mymobileproject.domain.model.Transaction
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.domain.model.TransactionType
import com.example.mymobileproject.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToReceiptScan: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.dashboard_this_month),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Summary Cards
            item { SummaryCard(uiState.summary.totalIncome, uiState.summary.totalExpense, uiState.summary.balance) }

            // AI Insight
            item {
                if (uiState.aiInsight.isNotEmpty()) {
                    AIInsightCard(uiState.aiInsight)
                }
            }

            // Category Breakdown
            item {
                if (uiState.summary.categoryBreakdown.isNotEmpty()) {
                    CategoryBreakdownCard(uiState.summary.categoryBreakdown, uiState.summary.totalExpense)
                }
            }

            // Daily Spending
            item {
                if (uiState.summary.dailySpending.isNotEmpty()) {
                    DailySpendingCard(uiState.summary.dailySpending)
                }
            }

            // Recent Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.dashboard_recent_transactions),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text(stringResource(R.string.dashboard_view_all))
                    }
                }
            }

            if (uiState.recentTransactions.isEmpty()) {
                item {
                    EmptyCard(
                        stringResource(R.string.transaction_empty),
                        stringResource(R.string.transaction_empty_subtitle)
                    )
                }
            } else {
                items(uiState.recentTransactions) { txn -> TransactionRow(txn) }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // FAB
        FloatingActionButton(
            onClick = onNavigateToAddTransaction,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Emerald500,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, "Add")
        }
    }
}

@Composable
private fun SummaryCard(income: Double, expense: Double, balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Emerald600, Emerald500, Blue500),
                        start = Offset(0f, 0f), end = Offset(1000f, 1000f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text("Balance", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                Text(
                    CurrencyUtils.formatBaht(balance),
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MiniStat("↑ ${stringResource(R.string.dashboard_income)}", CurrencyUtils.formatBaht(income), IncomeGreen)
                    MiniStat("↓ ${stringResource(R.string.dashboard_expense)}", CurrencyUtils.formatBaht(expense), ExpenseRed)
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AIInsightCard(insight: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Surface(shape = CircleShape, color = Emerald500.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("🤖", fontSize = 20.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.dashboard_ai_insight),
                    style = MaterialTheme.typography.labelLarge,
                    color = Emerald400
                )
                Spacer(Modifier.height(4.dp))
                Text(insight, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(breakdown: Map<TransactionCategory, Double>, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.dashboard_spending_by_category),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            breakdown.entries.sortedByDescending { it.value }.forEach { (cat, amount) ->
                val pct = if (total > 0) (amount / total).toFloat() else 0f
                CategoryBar(cat, amount, pct)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryBar(category: TransactionCategory, amount: Double, percentage: Float) {
    val color = getCategoryColor(category)
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
                Spacer(Modifier.width(8.dp))
                Text(getCatIcon(category), fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(category.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text(CurrencyUtils.formatBaht(amount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun DailySpendingCard(dailySpending: Map<Int, Double>) {
    val maxAmount = dailySpending.values.maxOrNull() ?: 1.0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.dashboard_daily_spending), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                val days = (1..java.time.LocalDate.now().dayOfMonth).toList().takeLast(14)
                for (day in days) {
                    val amount = dailySpending[day] ?: 0.0
                    val h = ((amount / maxAmount) * 80).toFloat().coerceAtLeast(4f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(8.dp).height(h.dp).clip(RoundedCornerShape(4.dp)).background(Emerald400))
                        Spacer(Modifier.height(4.dp))
                        Text("$day", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(txn: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = getCategoryColor(txn.category).copy(alpha = 0.15f), modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(getCatIcon(txn.category), fontSize = 18.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(txn.note.ifEmpty { txn.category.name }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(txn.date.toString(), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
            Text(
                "${if (txn.type == TransactionType.EXPENSE) "-" else "+"}${CurrencyUtils.formatBaht(txn.amount)}",
                color = if (txn.type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📝", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
    }
}

private fun getCategoryColor(cat: TransactionCategory) = when (cat) {
    TransactionCategory.FOOD -> CategoryFood
    TransactionCategory.TRANSPORT -> CategoryTransport
    TransactionCategory.SHOPPING -> CategoryShopping
    TransactionCategory.ENTERTAINMENT -> CategoryEntertainment
    TransactionCategory.BILLS -> CategoryBills
    TransactionCategory.HEALTH -> CategoryHealth
    TransactionCategory.EDUCATION -> CategoryEducation
    else -> CategoryOther
}

private fun getCatIcon(cat: TransactionCategory) = when (cat) {
    TransactionCategory.FOOD -> "🍔"
    TransactionCategory.TRANSPORT -> "🚗"
    TransactionCategory.SHOPPING -> "🛍️"
    TransactionCategory.ENTERTAINMENT -> "🎬"
    TransactionCategory.BILLS -> "📄"
    TransactionCategory.HEALTH -> "💊"
    TransactionCategory.EDUCATION -> "📚"
    TransactionCategory.SALARY -> "💵"
    TransactionCategory.FREELANCE -> "💻"
    TransactionCategory.OTHER -> "📦"
}
