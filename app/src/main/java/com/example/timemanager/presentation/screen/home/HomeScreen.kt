package com.example.timemanager.presentation.screen.home

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
import com.example.timemanager.domain.model.Priority
import com.example.timemanager.presentation.common.components.*
import com.example.timemanager.presentation.theme.TimeManagerTheme

/**
 * 首页屏幕
 *
 * @param onNavigateToTask 导航到任务详情
 * @param onNavigateToSettings 导航到设置
 * @param viewModel ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTask: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // AI输入对话框
    if (uiState.showAIInput) {
        AIInputDialog(
            inputText = uiState.aiInputText,
            isLoading = uiState.isAILoading,
            parsedTask = uiState.parsedTask,
            onInputChange = { viewModel.onEvent(HomeEvent.UpdateAIInput(it)) },
            onSubmit = { viewModel.onEvent(HomeEvent.SubmitAITask) },
            onConfirm = { viewModel.onEvent(HomeEvent.ConfirmParsedTask) },
            onDismiss = { viewModel.onEvent(HomeEvent.HideAIInput) }
        )
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                onRefresh = { viewModel.onEvent(HomeEvent.Refresh) },
                onSettings = onNavigateToSettings,
                onAIInput = { viewModel.onEvent(HomeEvent.ShowAIInput) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(HomeEvent.ShowAIInput) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI创建任务"
                )
            }
        }
    ) { padding ->
        HomeContent(
            uiState = uiState,
            onTaskClick = onNavigateToTask,
            onCompleteTask = { viewModel.onEvent(HomeEvent.CompleteTask(it)) },
            onRequestAI = { viewModel.onEvent(HomeEvent.RequestAISuggestion) },
            onDismissAI = { viewModel.onEvent(HomeEvent.DismissAISuggestion) },
            modifier = Modifier.padding(padding)
        )
    }
}

/**
 * AI输入对话框
 */
@Composable
fun AIInputDialog(
    inputText: String,
    isLoading: Boolean,
    parsedTask: com.example.timemanager.domain.model.ParsedTask?,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI创建任务") },
        text = {
            Column {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("描述你的任务，例如：明天下午3点开会，提前15分钟提醒") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                if (isLoading) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI正在解析...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                parsedTask?.let { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("解析结果:", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("标题: ${task.title}", style = MaterialTheme.typography.bodyMedium)
                            task.description?.let { Text("描述: $it", style = MaterialTheme.typography.bodySmall) }
                            task.durationMinutes?.let { Text("时长: $it 分钟", style = MaterialTheme.typography.bodySmall) }
                            task.priority?.let { p ->
                                val priorityName = when (p) {
                                    Priority.HIGH -> "高"
                                    Priority.MEDIUM -> "中"
                                    Priority.LOW -> "低"
                                }
                                Text("优先级: $priorityName", style = MaterialTheme.typography.bodySmall)
                            }
                            task.category?.let { Text("分类: $it", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when {
                parsedTask != null -> {
                    TextButton(onClick = onConfirm) {
                        Text("确认创建")
                    }
                }
                isLoading -> {
                    TextButton(onClick = {}, enabled = false) {
                        Text("解析中...")
                    }
                }
                else -> {
                    TextButton(onClick = onSubmit, enabled = inputText.isNotBlank()) {
                        Text("解析")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 首页顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    onAIInput: () -> Unit
) {
    TopAppBar(
        title = {
            Text("今日任务")
        },
        actions = {
            IconButton(onClick = onAIInput) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI创建任务"
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "刷新"
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置"
                )
            }
        }
    )
}

/**
 * 首页内容
 */
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onTaskClick: (String) -> Unit,
    onCompleteTask: (String) -> Unit,
    onRequestAI: () -> Unit,
    onDismissAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            ErrorStateView(
                message = uiState.error!!,
                onRetry = { /* TODO: 重试逻辑 */ },
                modifier = modifier
            )
        }

        !uiState.hasTasks -> {
            EmptyStateView(
                title = "暂无任务",
                message = "点击右下角按钮添加新任务",
                modifier = modifier,
                action = {
                    FilledTonalButton(onClick = onRequestAI) {
                        Text("获取AI建议")
                    }
                }
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 统计卡片
                item {
                    StatsCard(
                        pendingCount = uiState.pendingCount,
                        completedCount = uiState.completedCount,
                        onRequestAI = onRequestAI
                    )
                }

                // AI建议卡片
                if (uiState.showAISuggestion && uiState.aiSuggestion != null) {
                    item {
                        AISuggestionCard(
                            suggestion = uiState.aiSuggestion!!,
                            onDismiss = onDismissAI
                        )
                    }
                }

                // AI加载指示器
                if (uiState.isAILoading) {
                    item {
                        AILoadingIndicator(message = "AI正在分析您的任务...")
                    }
                }

                // 任务列表
                items(
                    items = uiState.tasks,
                    key = { it.id }
                ) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task.id) },
                        onComplete = { onCompleteTask(task.id) }
                    )
                }
            }
        }
    }
}

/**
 * 统计卡片
 */
@Composable
fun StatsCard(
    pendingCount: Int,
    completedCount: Int,
    onRequestAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 待办数量
            StatItem(
                label = "待办",
                value = pendingCount.toString(),
                color = MaterialTheme.colorScheme.primary
            )

            // 分隔线
            HorizontalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // 已完成数量
            StatItem(
                label = "已完成",
                value = completedCount.toString(),
                color = com.example.timemanager.presentation.theme.Success
            )

            // 分隔线
            HorizontalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // AI建议按钮
            TextButton(onClick = onRequestAI) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI建议")
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
