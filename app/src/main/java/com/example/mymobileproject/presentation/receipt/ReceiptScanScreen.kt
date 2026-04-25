package com.example.mymobileproject.presentation.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymobileproject.R
import com.example.mymobileproject.ui.theme.*

@Composable
fun ReceiptScanScreen(onNavigateBack: () -> Unit) {
    // Placeholder — will integrate CameraX + ML Kit in Phase 7
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(stringResource(R.string.receipt_scan_title), style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(Modifier.height(48.dp))

        // Camera preview placeholder
        Card(
            modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CameraAlt, null, tint = TextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("📸", fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.receipt_scan_capture),
                        style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("ML Kit Text Recognition",
                        style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    Spacer(Modifier.height(4.dp))
                    Text("Coming soon — use manual entry for now",
                        style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: CameraX capture + ML Kit processing */ },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            enabled = false
        ) {
            Text(stringResource(R.string.receipt_scan_capture), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
