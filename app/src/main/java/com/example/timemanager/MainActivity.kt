package com.example.timemanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.timemanager.presentation.navigation.AppNavigation
import com.example.timemanager.presentation.theme.TimeManagerTheme
import com.example.timemanager.service.worker.ReminderWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主Activity
 *
 * 应用入口，负责初始化UI和权限请求
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 权限请求启动器
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 处理权限结果
        permissions.forEach { (permission, granted) ->
            if (!granted) {
                // 用户拒绝了权限
                // TODO: 显示提示
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 请求必要权限
        requestPermissions()

        // 启动后台提醒服务
        ReminderWorker.schedule(this)

        setContent {
            TimeManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    /**
     * 请求必要权限
     */
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // 通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 如果有需要请求的权限
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
