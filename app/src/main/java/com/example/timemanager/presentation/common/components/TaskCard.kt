package com.example.timemanager.presentation.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.timemanager.domain.model.Priority
import com.example.timemanager.domain.model.Task
import com.example.timemanager.domain.model.TaskStatus
import com.example.timemanager.presentation.theme.*
import java.time.format.DateTimeFormatter

/**
 * 任务卡片组件
 *
 * 显示单个任务的信息
 *
 * @param task 任务数据
 * @param onClick 点击事件
 * @param onComplete 完成事件
 * @param modifier 修饰符
 */
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成按钮
            IconButton(
                onClick = onComplete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "完成",
                    tint = if (task.status == TaskStatus.COMPLETED) {
                        Success
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 任务信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 标题
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    },
                    color = if (task.status == TaskStatus.COMPLETED) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                // 描述
                task.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 底部信息行
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 优先级标签
                    PriorityChip(priority = task.priority)

                    // 截止时间
                    task.dueTime?.let { time ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = time.format(DateTimeFormatter.ofPattern("MM/dd HH:mm")),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.isOverdue()) {
                                Error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    // 分类
                    if (task.category.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = task.category,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            // 更多选项
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多选项"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            showMenu = false
                            // TODO: 删除逻辑
                        }
                    )
                }
            }
        }
    }
}

/**
 * 优先级标签
 */
@Composable
fun PriorityChip(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val (color, backgroundColor) = when (priority) {
        Priority.LOW -> PriorityLow to PriorityLow.copy(alpha = 0.2f)
        Priority.MEDIUM -> PriorityMedium to PriorityMedium.copy(alpha = 0.2f)
        Priority.HIGH -> PriorityHigh to PriorityHigh.copy(alpha = 0.2f)
        Priority.URGENT -> PriorityUrgent to PriorityUrgent.copy(alpha = 0.2f)
    }

    Surface(
        modifier = modifier.height(20.dp),
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * 任务状态标签
 */
@Composable
fun StatusChip(
    status: TaskStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        TaskStatus.TODO -> StatusTodo
        TaskStatus.IN_PROGRESS -> StatusInProgress
        TaskStatus.COMPLETED -> StatusCompleted
        TaskStatus.CANCELLED -> StatusCancelled
    }

    Surface(
        modifier = modifier.height(24.dp),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
