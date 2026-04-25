package com.example.mymobileproject.presentation.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
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

        OutlinedTextField(value = state.note, onValueChange = { viewModel.updateNote(it) },
            modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.transaction_note)) },
            shape = RoundedCornerShape(12.dp), singleLine = true)
        Spacer(Modifier.height(16.dp))

        // Paid By
        Text(stringResource(R.string.group_paid_by), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
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
                ) { Text(name, Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = if (selected) Blue400 else TextSecondary) }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Split type
        Text("Split Type", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(SplitType.EQUAL to stringResource(R.string.group_split_equal),
                SplitType.CUSTOM to stringResource(R.string.group_split_custom),
                SplitType.PERCENTAGE to stringResource(R.string.group_split_percentage)).forEach { (type, label) ->
                val sel = state.splitType == type
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { viewModel.updateSplitType(type) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (sel) Emerald500.copy(alpha = 0.15f) else DarkCard
                ) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = if (sel) Emerald400 else TextTertiary, style = MaterialTheme.typography.labelLarge) }
            }
        }

        // Custom splits (only for CUSTOM / PERCENTAGE)
        if (state.splitType != SplitType.EQUAL) {
            Spacer(Modifier.height(12.dp))
            group?.members?.forEach { uid ->
                val name = group.memberNames[uid] ?: uid
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, Modifier.weight(1f), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
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

        Spacer(Modifier.weight(1f))

        Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue500),
            enabled = state.amount.isNotBlank() && !state.isSaving) {
            if (state.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(stringResource(R.string.save), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
