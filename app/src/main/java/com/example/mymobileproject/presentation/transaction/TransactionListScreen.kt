package com.example.mymobileproject.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun TransactionListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToReceiptScan: () -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else if (state.transactions.isEmpty()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📝", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.transaction_empty), color = TextSecondary)
                Text(stringResource(R.string.transaction_empty_subtitle), color = TextTertiary, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            val grouped = state.transactions.groupBy { it.date }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(stringResource(R.string.nav_transactions), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(16.dp))
                }
                grouped.forEach { (date, txns) ->
                    item {
                        Text(date.toString(), style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    items(txns, key = { it.id }) { txn ->
                        SwipeTxnItem(txn) { viewModel.delete(txn.id) }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Emerald500, contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Filled.Add, "Add") }
    }
}

@Composable
private fun SwipeTxnItem(txn: Transaction, onDelete: () -> Unit) {
    val catColor = getCatColor(txn.category)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = catColor.copy(alpha = 0.15f), modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(getCatEmoji(txn.category), fontSize = 18.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(txn.note.ifEmpty { txn.category.name }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(txn.category.name.lowercase(), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
            Text(
                "${if (txn.type == TransactionType.EXPENSE) "-" else "+"}${CurrencyUtils.formatBaht(txn.amount)}",
                color = if (txn.type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, "Delete", tint = TextTertiary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun getCatColor(c: TransactionCategory) = when(c) {
    TransactionCategory.FOOD -> CategoryFood; TransactionCategory.TRANSPORT -> CategoryTransport
    TransactionCategory.SHOPPING -> CategoryShopping; TransactionCategory.ENTERTAINMENT -> CategoryEntertainment
    TransactionCategory.BILLS -> CategoryBills; TransactionCategory.HEALTH -> CategoryHealth
    TransactionCategory.EDUCATION -> CategoryEducation; else -> CategoryOther
}
private fun getCatEmoji(c: TransactionCategory) = when(c) {
    TransactionCategory.FOOD->"🍔"; TransactionCategory.TRANSPORT->"🚗"; TransactionCategory.SHOPPING->"🛍️"
    TransactionCategory.ENTERTAINMENT->"🎬"; TransactionCategory.BILLS->"📄"; TransactionCategory.HEALTH->"💊"
    TransactionCategory.EDUCATION->"📚"; TransactionCategory.SALARY->"💵"; TransactionCategory.FREELANCE->"💻"; TransactionCategory.OTHER->"📦"
}
