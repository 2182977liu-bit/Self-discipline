package com.example.timemanager.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    Scaffold(
        topBar = {
            HomeTopAppBar(
                onRefresh = { viewModel.onEvent(HomeEvent.Refresh) },
                onSettings = onNavigateToSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToTask("new") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加任务"
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
 * 首页顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    onRefresh: () -> Unit,
    onSettings: () -> Unit
) {
    TopAppBar(
        title = {
            Text("今日任务")
        },
        actions = {
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
