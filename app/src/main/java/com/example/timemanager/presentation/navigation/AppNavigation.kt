package com.example.timemanager.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.timemanager.presentation.screen.home.HomeScreen
import com.example.timemanager.presentation.screen.task.TaskDetailScreen
import com.example.timemanager.presentation.screen.task.TaskListScreen
import com.example.timemanager.presentation.screen.settings.SettingsScreen

/**
 * 应用导航配置
 *
 * @param navController 导航控制器
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // 只在主要页面显示底部导航栏
            if (currentRoute in listOf("home", "tasks", "ai", "settings")) {
                BottomNavigationBar(
                    currentRoute = currentRoute ?: "home",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // 避免重复导航到同一页面
                            launchSingleTop = true
                            // 恢复状态
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // 首页
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToTask = { taskId ->
                        navController.navigate(Screen.TaskDetail.createRoute(taskId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // 任务列表
            composable(Screen.TaskList.route) {
                TaskListScreen(
                    onNavigateToTask = { taskId ->
                        navController.navigate(Screen.TaskDetail.createRoute(taskId))
                    }
                )
            }

            // 任务详情
            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(
                    navArgument("taskId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: "new"
                TaskDetailScreen(
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 设置
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // AI助手（简化实现）
            composable(Screen.AIAssistant.route) {
                AIAssistantPlaceholder(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
            label = { Text("首页") }
        )
        NavigationBarItem(
            selected = currentRoute == "tasks",
            onClick = { onNavigate("tasks") },
            icon = { Icon(Icons.Default.Task, contentDescription = "任务") },
            label = { Text("任务") }
        )
        NavigationBarItem(
            selected = currentRoute == "ai",
            onClick = { onNavigate("ai") },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI助手") },
            label = { Text("AI") }
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
            label = { Text("设置") }
        )
    }
}

/**
 * AI助手占位页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantPlaceholder(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI助手") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AI智能助手",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请在设置中配置Kimi API密钥后使用",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
