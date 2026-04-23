package com.example.timemanager.presentation.screen.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timemanager.BuildConfig

/**
 * 设置屏幕
 *
 * @param onNavigateBack 返回回调
 * @param viewModel ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // API密钥设置
            SettingsSection(title = "AI设置") {
                OutlinedTextField(
                    value = uiState.kimiApiKey,
                    onValueChange = { viewModel.onEvent(SettingsEvent.UpdateApiKey(it)) },
                    label = { Text("Kimi API密钥") },
                    placeholder = { Text("sk-...") },
                    singleLine = true,
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { showApiKey = !showApiKey }) {
                            Text(if (showApiKey) "隐藏" else "显示")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (uiState.isApiKeyValid == true) {
                            Text("API密钥已配置", color = MaterialTheme.colorScheme.primary)
                        } else if (uiState.isApiKeyValid == false) {
                            Text("API密钥格式不正确", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("在 platform.moonshot.cn 获取API密钥")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.onEvent(SettingsEvent.ValidateApiKey) },
                        enabled = uiState.kimiApiKey.isNotBlank() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("验证密钥")
                        }
                    }

                    OutlinedButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.moonshot.cn/"))
                        context.startActivity(intent)
                    }) {
                        Text("获取密钥")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 通知设置
            SettingsSection(title = "通知设置") {
                SettingsSwitch(
                    title = "启用通知",
                    subtitle = "接收任务提醒和AI建议通知",
                    checked = uiState.notificationEnabled,
                    onCheckedChange = { 
                        viewModel.onEvent(SettingsEvent.UpdateNotificationEnabled(it))
                        if (it) {
                            // 请求通知权限
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                SettingsSwitch(
                    title = "通知声音",
                    subtitle = "播放提醒声音",
                    checked = uiState.soundEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.UpdateSoundEnabled(it)) }
                )

                SettingsSwitch(
                    title = "振动提醒",
                    subtitle = "收到通知时振动",
                    checked = uiState.vibrationEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.UpdateVibrationEnabled(it)) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 提醒设置
            SettingsSection(title = "提醒设置") {
                var expanded by remember { mutableStateOf(false) }
                val options = listOf(5, 10, 15, 30, 60)
                val labels = options.map { 
                    if (it >= 60) "${it / 60}小时" else "${it}分钟"
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = if (uiState.defaultReminderMinutes >= 60) 
                            "${uiState.defaultReminderMinutes / 60}小时"
                        else 
                            "${uiState.defaultReminderMinutes}分钟",
                        onValueChange = {},
                        label = { Text("默认提前提醒时间") },
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
                                text = { Text(labels[index]) },
                                onClick = {
                                    viewModel.onEvent(SettingsEvent.UpdateDefaultReminder(minutes))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 健康提醒设置
            SettingsSection(title = "健康提醒") {
                SettingsSwitch(
                    title = "喝水提醒",
                    subtitle = "定时提醒喝水",
                    checked = uiState.waterReminderEnabled,
                    onCheckedChange = { 
                        viewModel.onEvent(SettingsEvent.UpdateWaterReminder(it, uiState.waterReminderInterval))
                    }
                )

                if (uiState.waterReminderEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    val intervals = listOf(30, 45, 60, 90, 120)
                    val labels = intervals.map { "${it}分钟" }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${uiState.waterReminderInterval}分钟",
                            onValueChange = {},
                            label = { Text("提醒间隔") },
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
                            intervals.forEachIndexed { index, minutes ->
                                DropdownMenuItem(
                                    text = { Text(labels[index]) },
                                    onClick = {
                                        viewModel.onEvent(SettingsEvent.UpdateWaterReminder(true, minutes))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 关于
            SettingsSection(title = "关于") {
                ListItem(
                    headlineContent = { Text("版本") },
                    supportingContent = { Text("v${BuildConfig.VERSION_NAME}") }
                )

                ListItem(
                    headlineContent = { Text("AI智能时间管理") },
                    supportingContent = { Text("基于Kimi AI的智能任务管理应用") },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                )
            }

            // 提示消息
            uiState.message?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.onEvent(SettingsEvent.ClearMessage) }) {
                            Text("确定")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(message)
                }
            }
        }
    }
}

/**
 * 设置分组
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

/**
 * 设置开关项
 */
@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
