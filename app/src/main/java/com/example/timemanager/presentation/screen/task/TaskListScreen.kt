package com.example.timemanager.presentation.screen.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timemanager.domain.model.TaskStatus
import com.example.timemanager.presentation.common.components.EmptyStateView
import com.example.timemanager.presentation.common.components.TaskCard

/**
 * 任务列表屏幕
 *
 * @param onNavigateToTask 导航到任务详情
 * @param viewModel ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToTask: (String) -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<TaskStatus?>(null) }

    Scaffold(
        topBar = {
            TaskListTopAppBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { 
                    searchQuery = it
                    viewModel.onEvent(TaskEvent.Search(it))
                },
                selectedFilter = selectedFilter,
                onFilterChange = { 
                    selectedFilter = it
                    viewModel.onEvent(TaskEvent.FilterByStatus(it))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToTask("new") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            }
        } else if (uiState.tasks.isEmpty()) {
            EmptyStateView(
                title = "暂无任务",
                message = if (searchQuery.isNotEmpty()) "未找到匹配的任务" else "点击右下角按钮添加新任务",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.tasks,
                    key = { it.id }
                ) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onNavigateToTask(task.id) },
                        onComplete = { 
                            viewModel.onEvent(TaskEvent.DeleteTask(task.id))
                        }
                    )
                }
            }
        }
    }
}

/**
 * 任务列表顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: TaskStatus?,
    onFilterChange: (TaskStatus?) -> Unit
) {
    var showSearch by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("搜索任务") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("任务列表")
            }
        },
        actions = {
            IconButton(onClick = { showSearch = !showSearch }) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
        }
    )
}
