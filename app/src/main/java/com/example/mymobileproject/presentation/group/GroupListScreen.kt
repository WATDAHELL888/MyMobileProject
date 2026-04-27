package com.example.mymobileproject.presentation.group

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mymobileproject.R
import com.example.mymobileproject.core.util.CurrencyUtils
import com.example.mymobileproject.ui.theme.*

@Composable
fun GroupListScreen(
    onNavigateToGroup: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: GroupListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show seed message
    LaunchedEffect(state.seedMessage) {
        state.seedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSeedMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (state.groups.isEmpty() && !state.isLoading) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👥", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.group_empty), color = TextSecondary)
                Text(stringResource(R.string.group_empty_subtitle), color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(24.dp))
                // Mock data button
                OutlinedButton(
                    onClick = { viewModel.seedMockData() },
                    enabled = !state.isSeeding,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSeeding) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Emerald400)
                        Spacer(Modifier.width(8.dp))
                        Text("Creating mock data...", color = TextSecondary)
                    } else {
                        Text("🧪", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Load Demo Data", color = Emerald400)
                    }
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.group_title), style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                        // Seed button always visible
                        OutlinedButton(
                            onClick = { viewModel.seedMockData() },
                            enabled = !state.isSeeding,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (state.isSeeding) {
                                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Emerald400)
                            } else {
                                Text("🧪 Demo", style = MaterialTheme.typography.labelMedium, color = Emerald400)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                items(state.groups) { group ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToGroup(group.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Blue500.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("👥", fontSize = 22.sp) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(group.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${group.members.size} members", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        // Snackbar
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp))

        FloatingActionButton(
            onClick = onNavigateToCreate,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Blue500, contentColor = Color.White, shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Filled.Add, "Create Group") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onNavigateBack() }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(stringResource(R.string.group_create), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = state.name, onValueChange = { viewModel.updateName(it) },
            modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.group_name)) }, shape = RoundedCornerShape(12.dp), singleLine = true)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.group_members), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = state.newMember, onValueChange = { viewModel.updateNewMember(it) },
                modifier = Modifier.weight(1f), placeholder = { Text(stringResource(R.string.group_add_member)) },
                shape = RoundedCornerShape(12.dp), singleLine = true)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.addMember() }) {
                Icon(Icons.Filled.PersonAdd, "Add", tint = Emerald400)
            }
        }
        Spacer(Modifier.height(8.dp))
        state.memberNames.forEach { name ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("👤", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(name, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { viewModel.removeMember(name) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, "Remove", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue500),
            enabled = state.name.isNotBlank() && !state.isSaving) {
            if (state.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(stringResource(R.string.save), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
