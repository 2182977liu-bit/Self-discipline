package com.example.timemanager.presentation.screen.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timemanager.domain.model.Priority
import com.example.timemanager.domain.model.TaskCategory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 任务详情屏幕
 *
 * @param taskId 任务ID，"new"表示新建
 * @param onNavigateBack 返回回调
 * @param viewModel ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // AI解析输入
    var aiInput by remember { mutableStateOf("") }
    var showAIParseSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TaskDetailTopAppBar(
                isNewTask = uiState.isNewTask,
                isSaving = uiState.isSaving,
                onNavigateBack = onNavigateBack,
                onSave = { viewModel.onEvent(TaskEvent.SaveTask) },
                onDelete = if (!uiState.isNewTask) {
                    { showDeleteDialog = true }
                } else null
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI解析按钮
            if (uiState.isNewTask) {
                OutlinedButton(
                    onClick = { showAIParseSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("使用AI解析任务")
                }
            }

            // 标题输入
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onEvent(TaskEvent.UpdateTitle(it)) },
                label = { Text("任务标题 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.title.isBlank() && uiState.error != null
            )

            // 描述输入
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onEvent(TaskEvent.UpdateDescription(it)) },
                label = { Text("任务描述") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // 截止时间
            DateTimePicker(
                dateTime = uiState.dueTime,
                onDateTimeChange = { viewModel.onEvent(TaskEvent.UpdateDueTime(it)) },
                label = "截止时间"
            )

            // 时长选择
            DurationPicker(
                durationMinutes = uiState.durationMinutes,
                onDurationChange = { viewModel.onEvent(TaskEvent.UpdateDuration(it)) }
            )

            // 优先级选择
            PrioritySelector(
                priority = uiState.priority,
                onPriorityChange = { viewModel.onEvent(TaskEvent.UpdatePriority(it)) }
            )

            // 分类选择
            CategorySelector(
                category = uiState.category,
                onCategoryChange = { viewModel.onEvent(TaskEvent.UpdateCategory(it)) }
            )

            // 提醒时间
            ReminderSelector(
                reminderMinutes = uiState.reminderMinutes,
                onReminderChange = { viewModel.onEvent(TaskEvent.UpdateReminder(it)) }
            )

            // 错误提示
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除任务") },
            text = { Text("确定要删除这个任务吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.task?.let { 
                            viewModel.onEvent(TaskEvent.DeleteTask(it.id))
                        }
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 任务详情顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailTopAppBar(
    isNewTask: Boolean,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?
) {
    TopAppBar(
        title = { Text(if (isNewTask) "新建任务" else "编辑任务") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
            IconButton(
                onClick = onSave,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = "保存")
                }
            }
        }
    )
}

/**
 * 日期时间选择器
 */
@Composable
fun DateTimePicker(
    dateTime: LocalDateTime?,
    onDateTimeChange: (LocalDateTime?) -> Unit,
    label: String
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

    OutlinedTextField(
        value = dateTime?.format(formatter) ?: "未设置",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            Row {
                if (dateTime != null) {
                    TextButton(onClick = { onDateTimeChange(null) }) {
                        Text("清除")
                    }
                }
                TextButton(onClick = {
                    val now = dateTime ?: LocalDateTime.now()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    onDateTimeChange(
                                        LocalDateTime.of(year, month + 1, day, hour, minute)
                                    )
                                },
                                now.hour,
                                now.minute,
                                true
                            ).show()
                        },
                        now.year,
                        now.monthValue - 1,
                        now.dayOfMonth
                    ).show()
                }) {
                    Text("选择")
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * 时长选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationPicker(
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(15, 30, 45, 60, 90, 120, 180)
    val displayValues = options.map { 
        if (it >= 60) "${it / 60}小时${if (it % 60 > 0) "${it % 60}分钟" else ""}"
        else "${it}分钟"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = if (durationMinutes >= 60) {
                "${durationMinutes / 60}小时${if (durationMinutes % 60 > 0) "${durationMinutes % 60}分钟" else ""}"
            } else {
                "${durationMinutes}分钟"
            },
            onValueChange = {},
            label = { Text("预计时长") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, minutes ->
                DropdownMenuItem(
                    text = { Text(displayValues[index]) },
                    onClick = {
                        onDurationChange(minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 优先级选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySelector(
    priority: Priority,
    onPriorityChange: (Priority) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = priority.label,
            onValueChange = {},
            label = { Text("优先级") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Priority.entries.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.label) },
                    onClick = {
                        onPriorityChange(p)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 分类选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    category: String,
    onCategoryChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = TaskCategory.entries

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = category.ifEmpty { "选择分类" },
            onValueChange = {},
            label = { Text("分类") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.label) },
                    onClick = {
                        onCategoryChange(c.label)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 提醒时间选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSelector(
    reminderMinutes: Int,
    onReminderChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(0, 5, 10, 15, 30, 60, 120)
    val displayValues = listOf("不提醒", "提前5分钟", "提前10分钟", "提前15分钟", "提前30分钟", "提前1小时", "提前2小时")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayValues[options.indexOf(reminderMinutes).takeIf { it >= 0 } ?: 0],
            onValueChange = {},
            label = { Text("提醒时间") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, minutes ->
                DropdownMenuItem(
                    text = { Text(displayValues[index]) },
                    onClick = {
                        onReminderChange(minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}
