package com.example.mymobileproject.presentation.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
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
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.domain.model.TransactionType
import com.example.mymobileproject.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onNavigateBack() }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(stringResource(R.string.transaction_add), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(Modifier.height(16.dp))

        // Quick Add & Scan
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.quickAddText,
                onValueChange = { viewModel.quickAdd(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.transaction_quick_add)) },
                leadingIcon = { Icon(Icons.Filled.FlashOn, null, tint = Emerald400) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Button(
                onClick = { viewModel.scanReceiptMock() },
                enabled = !state.isScanning,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500.copy(alpha = 0.15f), contentColor = Emerald500),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = Emerald500)
                } else {
                    Icon(Icons.Filled.CameraAlt, "Scan")
                    Spacer(Modifier.width(4.dp))
                    Text("Scan")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Type toggle
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(TransactionType.EXPENSE to stringResource(R.string.transaction_type_expense),
                   TransactionType.INCOME to stringResource(R.string.transaction_type_income)).forEach { (type, label) ->
                val selected = state.type == type
                Surface(
                    modifier = Modifier.weight(1f).clickable { viewModel.updateType(type) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) (if (type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen) else DarkCard
                ) {
                    Text(label, modifier = Modifier.padding(12.dp), color = if (selected) Color.White else TextSecondary,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Amount
        OutlinedTextField(
            value = state.amount,
            onValueChange = { viewModel.updateAmount(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.transaction_amount)) },
            prefix = { Text("฿ ", fontWeight = FontWeight.Bold, color = Emerald400) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Note
        OutlinedTextField(
            value = state.note,
            onValueChange = { viewModel.updateNote(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.transaction_note)) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        // Category selector
        Text(stringResource(R.string.transaction_category), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))

        val categories = if (state.type == TransactionType.EXPENSE)
            listOf(TransactionCategory.FOOD, TransactionCategory.TRANSPORT, TransactionCategory.SHOPPING,
                TransactionCategory.ENTERTAINMENT, TransactionCategory.BILLS, TransactionCategory.HEALTH,
                TransactionCategory.EDUCATION, TransactionCategory.OTHER)
        else listOf(TransactionCategory.SALARY, TransactionCategory.FREELANCE, TransactionCategory.OTHER)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.heightIn(max = 200.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val selected = state.category == cat
                val emoji = when(cat) {
                    TransactionCategory.FOOD->"🍔"; TransactionCategory.TRANSPORT->"🚗"
                    TransactionCategory.SHOPPING->"🛍️"; TransactionCategory.ENTERTAINMENT->"🎬"
                    TransactionCategory.BILLS->"📄"; TransactionCategory.HEALTH->"💊"
                    TransactionCategory.EDUCATION->"📚"; TransactionCategory.SALARY->"💵"
                    TransactionCategory.FREELANCE->"💻"; TransactionCategory.OTHER->"📦"
                }
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { viewModel.updateCategory(cat) }
                        .then(if (selected) Modifier.border(2.dp, Emerald400, RoundedCornerShape(12.dp)) else Modifier),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) Emerald500.copy(alpha = 0.15f) else DarkCard
                ) {
                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 24.sp)
                        Text(cat.name.take(6).lowercase(), style = MaterialTheme.typography.labelSmall, color = if (selected) Emerald400 else TextTertiary, maxLines = 1)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Save button
        Button(
            onClick = { viewModel.save() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            enabled = !state.isSaving && state.amount.isNotEmpty()
        ) {
            if (state.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(stringResource(R.string.transaction_save), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
        }
    }
}
