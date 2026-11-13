package com.owen.primrosemotorcycles.userInterface.jobcardList


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.data.JobStatus
import java.text.SimpleDateFormat
import java.util.*
import com.owen.primrosemotorcycles.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardListScreen(
    uiState: JobListUiState,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onEvent: (JobListEvent) -> Unit
) {
    Scaffold(

        topBar = {
            if (windowWidthSizeClass == WindowWidthSizeClass.Compact) {
                TopAppBar(
                    colors = TopAppBarColors(
                        containerColor = Color.Green,
                        scrolledContainerColor = Color.Blue,
                        navigationIconContentColor = Color.Blue,
                        titleContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    ),
                    title = {
                        Image(
                            painter = painterResource(R.drawable.primrose_logo),
                            contentDescription = stringResource(R.string.app_title_primrose),
                            alignment = Alignment.Center,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    },
                    actions = {
                        IconButton(onClick = { onEvent(JobListEvent.StockClicked) }) {
                            Icon(Icons.Default.Inventory2, contentDescription = "Stock Management")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(JobListEvent.AddClicked) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Job Card") }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(12.dp)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(JobListEvent.SearchChanged(it)) },
                placeholder = { Text("Search reg, name, job no...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.selectedStatus == null,
                        onClick = { onEvent(JobListEvent.StatusSelected(null)) },
                        label = { Text("All (${uiState.jobs.size})") }
                    )
                }
                items(JobStatus.entries) { status ->
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = { onEvent(JobListEvent.StatusSelected(status)) },
                        label = { Text(status.name.replace("_", " ")) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.jobs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No job cards")
                        Button(onClick = { onEvent(JobListEvent.AddClicked) }) { Text("Create Job Card") }
                    }
                }
            } else {
                if (windowWidthSizeClass == WindowWidthSizeClass.Compact) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.jobs, key = { it.id }) { job ->
                            JobCardItem(job) { onEvent(JobListEvent.CardClicked(job.id)) }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(360.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.jobs, key = { it.id }) { job ->
                            JobCardItem(job) { onEvent(JobListEvent.CardClicked(job.id)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobCardItem(job: JobCard, onClick: () -> Unit) {
    val fmt = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val color = when (job.getStatusEnum()) {
        JobStatus.OPEN -> MaterialTheme.colorScheme.primary
        JobStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
        JobStatus.WAITING_PARTS -> MaterialTheme.colorScheme.error
        JobStatus.DONE -> MaterialTheme.colorScheme.secondary
        JobStatus.COLLECTED -> MaterialTheme.colorScheme.outline
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(job.jobNumber, fontWeight = FontWeight.Bold)
                Badge(containerColor = color) { Text(job.status, Modifier.padding(horizontal = 4.dp)) }
            }
            Text("${job.bikeReg} • ${job.bikeModel}", style = MaterialTheme.typography.titleSmall)
            Text(job.customerName, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(fmt.format(Date(job.createdAt)), style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (job.checklistDone) Text("✓ Checklist", style = MaterialTheme.typography.labelSmall)
                    if (job.quotationDone) Text("✓ Quote", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}