package com.example.cloudapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudapp.data.Chores
import com.example.cloudapp.ui.theme.ChoreViewModel
import com.example.cloudapp.ui.theme.CloudAppTheme
import com.example.cloudapp.ui.theme.DownloadStatus
import com.example.cloudapp.ui.theme.UiState
import com.example.cloudapp.ui.theme.UploadStatus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CloudAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel:ChoreViewModel = viewModel()
                    val uiState = viewModel.uiState.collectAsState().value
                    ChoreScreen(state = uiState, onEvent = viewModel::onEvent)
                }
            }
        }
    }
}

sealed interface ChoreEvent {
    object OnSaveClicked : ChoreEvent
    object OnRefreshClicked : ChoreEvent
    data class OnNameEdit(val name: String) : ChoreEvent
    data class OnItemDelete(val chore: Chores) : ChoreEvent
}

@Composable
fun ChoreScreen(
    state: UiState,
    onEvent: (ChoreEvent) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    label = { Text(text = "Add a chore") },
                    placeholder = { Text(text = "Throw the thrash") },
                    value = state.name,
                    onValueChange = { onEvent(ChoreEvent.OnNameEdit(it)) },
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = { onEvent(ChoreEvent.OnSaveClicked) },
                    enabled = state.name.isNotBlank()
                )
                {
                    Text(text = "Save")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
                if (state.uploadStatus == UploadStatus.IN_PROGRESS) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = "All Chores",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    if (state.downloadStatus == DownloadStatus.IN_PROGRESS) {
                        CircularProgressIndicator(strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(24.dp))
                    }
                }
                IconButton(onClick = { onEvent(ChoreEvent.OnRefreshClicked) }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "refresh")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {

                when (state.downloadStatus) {
                    DownloadStatus.IN_PROGRESS -> {
                        Box(modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    }

                    DownloadStatus.SUCCESS -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.choresList) { chore ->
                                ChoreCardItem(
                                    chore = chore,
                                    onDelete = { onEvent(ChoreEvent.OnItemDelete(chore)) }
                                )
                            }
                        }
                    }

                    DownloadStatus.FAILURE -> {
                        Text(
                            text = "Failed to fetch chores",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoreCardItem(chore: Chores, onDelete: () -> Unit) {
    Card(onClick = {}) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = chore.name, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardPreview() {
    CloudAppTheme {
        ChoreCardItem(chore = Chores("Throw the trash")) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ChoreScreenPreview() {
    CloudAppTheme {
        ChoreScreen(state = UiState())
    }
}