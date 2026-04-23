package com.example.timemanager.presentation.screen.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = uiState.selectedTaskIds.size,
                    totalCount = uiState.tasks.size,
                    onSelectAll = { viewModel.onEvent(TaskEvent.SelectAllTasks) },
                    onDelete = { viewModel.onEvent(TaskEvent.DeleteSelectedTasks) },
                    onExit = { viewModel.onEvent(TaskEvent.ExitSelectionMode) }
                )
            } else {
                TaskListTopAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = {
                        searchQuery = it
                        viewModel.onEvent(TaskEvent.Search(it))
                    }
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                FloatingActionButton(
                    onClick = { onNavigateToTask("new") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加任务")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
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
                    TaskListItem(
                        task = task,
                        isSelected = task.id in uiState.selectedTaskIds,
                        isSelectionMode = uiState.isSelectionMode,
                        onClick = {
                            if (uiState.isSelectionMode) {
                                viewModel.onEvent(TaskEvent.ToggleTaskSelection(task.id))
                            } else {
                                onNavigateToTask(task.id)
                            }
                        },
                        onLongClick = {
                            if (!uiState.isSelectionMode) {
                                viewModel.onEvent(TaskEvent.EnterSelectionMode)
                                viewModel.onEvent(TaskEvent.ToggleTaskSelection(task.id))
                            }
                        },
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
 * 选择模式顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onExit: () -> Unit
) {
    TopAppBar(
        title = { Text("已选择 $selectedCount / $totalCount 项") },
        navigationIcon = {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "取消选择")
            }
        },
        actions = {
            TextButton(onClick = onSelectAll) {
                Text("全选")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除选中",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

/**
 * 任务列表项（支持选择模式）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListItem(
    task: com.example.timemanager.domain.model.Task,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge
                )
                task.dueTime?.let {
                    Text(
                        text = it.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isSelectionMode) {
                IconButton(onClick = onComplete) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "完成",
                        tint = MaterialTheme.colorScheme.primary
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
    onSearchQueryChange: (String) -> Unit
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
