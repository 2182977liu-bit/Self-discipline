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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timemanager.domain.model.CheckInType
import com.example.timemanager.domain.model.PlanItem
import com.example.timemanager.domain.model.PlanType

/**
 * AI 生活管家首页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTaskList: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 目标输入对话框
    if (uiState.showGoalInput) {
        GoalInputDialog(
            goalText = uiState.goalText,
            isLoading = uiState.isAILoading,
            onGoalTextChange = { viewModel.onEvent(HomeEvent.UpdateGoalText(it)) },
            onGenerate = { viewModel.onEvent(HomeEvent.GeneratePlan) },
            onDismiss = { viewModel.onEvent(HomeEvent.HideGoalInput) }
        )
    }

    // 打卡确认对话框
    if (uiState.showCheckInDialog) {
        CheckInDialog(
            onCheckIn = { type -> viewModel.onEvent(HomeEvent.DoCheckIn(type)) },
            onDismiss = { viewModel.onEvent(HomeEvent.HideCheckInDialog) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 生活管家") },
                actions = {
                    // 天气信息
                    if (uiState.weatherInfo.isNotEmpty()) {
                        Text(
                            text = uiState.weatherInfo,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(HomeEvent.ShowGoalInput) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "设置目标")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 目标卡片
            if (uiState.currentGoal.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🎯 我的目标", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                uiState.currentGoal,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 今日状态概览
            item {
                TodayStatusCard(
                    steps = uiState.todaySteps,
                    checkInCount = uiState.todayCheckIns.size,
                    weatherInfo = uiState.weatherInfo,
                    onRefreshWeather = { viewModel.onEvent(HomeEvent.RefreshWeather) }
                )
            }

            // 快捷打卡按钮
            item {
                QuickCheckInButtons(
                    onCheckIn = { viewModel.onEvent(HomeEvent.ShowCheckInDialog) }
                )
            }

            // 查看任务按钮
            item {
                OutlinedButton(
                    onClick = onNavigateToTaskList,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("查看任务列表")
                }
            }

            // AI 加载中
            if (uiState.isAILoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("AI 正在为你规划今天...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // 今日计划
            if (uiState.planItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📋 今日计划",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            TextButton(onClick = { viewModel.onEvent(HomeEvent.ShowGoalInput) }) {
                                Text("重新生成")
                            }
                            TextButton(onClick = { viewModel.onEvent(HomeEvent.ClearPlan) }) {
                                Text("清除", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                items(uiState.planItems) { item ->
                    PlanItemCard(item = item)
                }
            }

            // 没有计划时的空状态
            if (uiState.planItems.isEmpty() && !uiState.isAILoading) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✨", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "告诉 AI 你的目标",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "例如：未来一周学习C++，早睡早起，轻度运动改善健康",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = { viewModel.onEvent(HomeEvent.ShowGoalInput) }
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开始规划")
                            }
                        }
                    }
                }
            }

            // 错误提示
            uiState.error?.let { error ->
                item {
                    Snackbar(
                        action = {
                            TextButton(onClick = { viewModel.onEvent(HomeEvent.ClearError) }) {
                                Text("关闭")
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }
}

/**
 * 今日状态卡片
 */
@Composable
fun TodayStatusCard(
    steps: Int,
    checkInCount: Int,
    weatherInfo: String,
    onRefreshWeather: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusItem(label = "今日步数", value = "$steps 步")
            StatusItem(label = "今日打卡", value = "$checkInCount 次")
            StatusItem(
                label = "天气",
                value = weatherInfo.ifEmpty { "点击刷新" },
                onClick = if (weatherInfo.isEmpty()) onRefreshWeather else null
            )
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.then(
            Modifier.padding(4.dp)
        ) else Modifier
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * 快捷打卡按钮
 */
@Composable
fun QuickCheckInButtons(onCheckIn: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("快捷打卡", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CheckInChip(icon = "🌙", label = "入睡", onClick = onCheckIn)
                CheckInChip(icon = "☀️", label = "醒来", onClick = onCheckIn)
                CheckInChip(icon = "🏃", label = "运动", onClick = onCheckIn)
                CheckInChip(icon = "🍽️", label = "吃饭", onClick = onCheckIn)
                CheckInChip(icon = "📖", label = "学习", onClick = onCheckIn)
            }
        }
    }
}

@Composable
fun CheckInChip(icon: String, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/**
 * 计划项卡片
 */
@Composable
fun PlanItemCard(item: PlanItem) {
    val typeColor = when (item.type) {
        PlanType.SLEEP, PlanType.WAKE_UP -> MaterialTheme.colorScheme.tertiary
        PlanType.MEAL -> MaterialTheme.colorScheme.secondary
        PlanType.EXERCISE -> MaterialTheme.colorScheme.primary
        PlanType.STUDY -> MaterialTheme.colorScheme.tertiaryContainer
        PlanType.REST -> MaterialTheme.colorScheme.surfaceVariant
        PlanType.OTHER -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间
            Text(
                item.time,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(50.dp)
            )

            // 类型色条
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp),
                color = typeColor
            ) {}

            Spacer(modifier = Modifier.width(12.dp))

            // 标题和备注
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyLarge)
                if (item.note.isNotEmpty()) {
                    Text(
                        item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 时长
            if (item.duration > 0) {
                Text(
                    "${item.duration}分钟",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 目标输入对话框
 */
@Composable
fun GoalInputDialog(
    goalText: String,
    isLoading: Boolean,
    onGoalTextChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设定你的目标") },
        text = {
            OutlinedTextField(
                value = goalText,
                onValueChange = onGoalTextChange,
                placeholder = { Text("例如：未来一周学习C++，早睡早起，轻度运动改善健康") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        },
        confirmButton = {
            Button(
                onClick = onGenerate,
                enabled = goalText.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("生成计划")
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
 * 打卡选择对话框
 */
@Composable
fun CheckInDialog(
    onCheckIn: (CheckInType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("打卡") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CheckInType.entries.forEach { type ->
                    Card(
                        onClick = { onCheckIn(type); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(type.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
