package com.example.mymobileproject.presentation.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mymobileproject.R
import com.example.mymobileproject.domain.model.AIMessage
import com.example.mymobileproject.domain.model.AIRole
import com.example.mymobileproject.ui.theme.*

@Composable
fun AIChatScreen(viewModel: AIChatViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Surface(color = DarkCard, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Emerald500.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("🤖", fontSize = 20.sp) }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.ai_chat_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Online", style = MaterialTheme.typography.labelSmall, color = Emerald400)
                }
            }
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.messages) { msg -> ChatBubble(msg) }

            // Typing indicator
            if (state.isTyping) {
                item { TypingIndicator() }
            }

            // Suggestions (show only if few messages)
            if (state.messages.size <= 2) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("💡 Suggestions", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val suggestions = listOf(
                            stringResource(R.string.ai_chat_suggestion_1),
                            stringResource(R.string.ai_chat_suggestion_2),
                            stringResource(R.string.ai_chat_suggestion_3),
                            stringResource(R.string.ai_chat_suggestion_4)
                        )
                        items(suggestions) { suggestion ->
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { viewModel.sendSuggestion(suggestion) },
                                shape = RoundedCornerShape(20.dp),
                                color = Emerald500.copy(alpha = 0.1f)
                            ) {
                                Text(suggestion, Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    color = Emerald400, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Input
        Surface(color = DarkCard, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.ai_chat_placeholder), color = TextTertiary) },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.send() },
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(
                        if (state.input.isNotBlank()) Emerald500 else DarkSurfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.Send, "Send",
                        tint = if (state.input.isNotBlank()) Color.White else TextTertiary,
                        modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: AIMessage) {
    val isUser = msg.role == AIRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(shape = CircleShape, color = Emerald500.copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("🤖", fontSize = 14.sp) }
            }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) Blue500 else DarkCard,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                msg.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = Emerald500.copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("🤖", fontSize = 14.sp) }
        }
        Spacer(Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = DarkCard) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { i ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = i * 200),
                            repeatMode = RepeatMode.Reverse
                        ), label = "dot$i"
                    )
                    Box(Modifier.size(8.dp).clip(CircleShape).background(TextTertiary.copy(alpha = alpha)))
                }
            }
        }
    }
}
