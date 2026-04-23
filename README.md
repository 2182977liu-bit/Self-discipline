# Self-discipline

AI 智能时间管理 APP — 基于大语言模型的智能任务管理应用

## 功能特性

### 🤖 AI 智能助手
- **自然语言创建任务** — 输入"明天下午3点开会，提前15分钟提醒"，AI 自动解析标题、时间、优先级
- **多 AI 提供商支持** — Kimi、DeepSeek、通义千问、豆包、智谱 GLM、自定义（OpenAI 兼容）
- **智能建议** — AI 根据任务列表提供时间管理优化建议
- **时间冲突检测** — 自动发现日程重叠并给出调整建议

### 📋 任务管理
- 任务增删改查、优先级（低/中/高/紧急）、分类
- 批量选择与删除（长按进入选择模式）
- 搜索与筛选
- 任务状态管理（待办/进行中/已完成/取消）

### ⏰ 智能提醒
- 自定义提前提醒时间（5/10/15/30/60 分钟）
- 通知、声音、振动提醒
- 健康提醒（喝水定时提醒）

### 🎨 界面
- Material Design 3 (Material You) 风格
- 深色/浅色/跟随系统主题
- Compose 声明式 UI

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| 依赖注入 | Hilt |
| 数据库 | Room (KSP) |
| 网络 | Retrofit + OkHttp + Gson |
| 异步 | Kotlin Coroutines + Flow |
| AI 接口 | OpenAI 兼容 `/v1/chat/completions` |
| CI/CD | GitHub Actions |

## 项目结构

```
app/src/main/java/com/example/timemanager/
├── data/                    # 数据层
│   ├── local/               # 本地存储 (Room, DataStore)
│   ├── remote/              # 网络请求 (Retrofit, DTO)
│   ├── mapper/              # Entity ↔ Domain 映射
│   └── repository/          # Repository 实现
├── domain/                  # 领域层
│   ├── model/               # 领域模型 (Task, AIProvider...)
│   ├── repository/          # Repository 接口
│   └── usecase/             # 用例 (AI解析, 任务CRUD...)
├── presentation/            # 表现层
│   ├── screen/              # 各页面 (首页, 任务, 设置...)
│   ├── navigation/          # 导航路由
│   ├── theme/               # 主题样式
│   └── common/              # 通用组件
├── di/                      # Hilt 依赖注入模块
├── service/                 # Android 服务 (闹钟, 通知, Worker)
└── utils/                   # 工具类
```

## 支持的 AI 提供商

| 提供商 | 平台 | 默认模型 |
|--------|------|---------|
| Kimi (月之暗面) | [platform.moonshot.cn](https://platform.moonshot.cn/) | moonshot-v1-8k |
| DeepSeek (深度求索) | [platform.deepseek.com](https://platform.deepseek.com/) | deepseek-chat |
| 通义千问 (阿里) | [dashscope.console.aliyun.com](https://dashscope.console.aliyun.com/) | qwen-turbo |
| 豆包 (字节跳动) | [console.volcengine.com/ark](https://console.volcengine.com/ark) | doubao-pro-32k |
| 智谱 GLM | [open.bigmodel.cn](https://open.bigmodel.cn/) | glm-4-flash |
| 自定义 | 任意 OpenAI 兼容 API | 自定义 |

## 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Android SDK 34

### 构建步骤

```bash
# 克隆项目
git clone https://github.com/2182977liu-bit/Self-discipline.git
cd Self-discipline

# 构建 Debug APK
./gradlew assembleDebug

# APK 输出路径
# app/build/outputs/apk/debug/app-debug.apk
```

### 使用 AI 功能

1. 打开 APP → 设置 → AI 设置
2. 选择 AI 提供商（推荐 DeepSeek 或 Kimi）
3. 输入对应平台的 API 密钥
4. 返回首页，点击 ✨ 按钮即可用自然语言创建任务

---

## 📦 如何生成 APK 安装包

以下提供三种方式，从简单到进阶：

### 方式一：从 GitHub Actions 直接下载（推荐，无需电脑环境）

每次推送代码到 `main` 分支会自动构建，直接下载即可：

1. 打开项目页面 → 点击顶部 **Actions** 标签
2. 点击最新的 **Build Android APK** 工作流
3. 滚动到底部 **Artifacts** 区域
4. 下载 `app-debug.apk`
5. 传到手机 → 安装（需开启"允许安装未知来源应用"）

> ⚠️ Debug APK 体积较大，仅供测试使用。如需发布，请使用方式三生成 Release APK。

### 方式二：在电脑上用 Android Studio 构建

适合需要修改代码后立即测试的场景。

**准备工作：**
1. 下载 [Android Studio](https://developer.android.com/studio)
2. 安装 JDK 17（Android Studio 自带）
3. 克隆项目：`git clone https://github.com/2182977liu-bit/Self-discipline.git`

**构建步骤：**
1. 用 Android Studio 打开项目根目录（`Self-discipline` 文件夹）
2. 等待 Gradle 同步完成（首次可能需要 5-10 分钟）
3. 菜单栏 → **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
4. 右下角弹出通知 → 点击 **locate** → 找到 `app-debug.apk`

### 方式三：命令行构建 Release APK（进阶）

适合 CI/CD 或批量构建。

```bash
# 1. 克隆项目
git clone https://github.com/2182977liu-bit/Self-discipline.git
cd Self-discipline

# 2. 构建 Debug APK（无需签名）
./gradlew assembleDebug
# 输出: app/build/outputs/apk/debug/app-debug.apk

# 3. 构建 Release APK（需要签名配置）
# 先创建签名密钥（只需一次）：
keytool -genkey -v -keystore release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias timemanager

# 然后在 app/build.gradle.kts 中配置 signingConfigs
# 最后执行：
./gradlew assembleRelease
# 输出: app/build/outputs/apk/release/app-release.apk
```

**Release APK vs Debug APK 对比：**

| | Debug APK | Release APK |
|--|-----------|-------------|
| 体积 | 较大（~30MB+） | 较小（~10MB） |
| 性能 | 未优化 | 已优化（混淆、压缩） |
| 调试信息 | 包含 | 已移除 |
| 签名 | Debug 签名 | 自定义签名 |
| 用途 | 开发测试 | 分发发布 |

---

## 📝 更新日志

### v1.0.1 (2026-04-23)

**新增功能：**
- 🤖 支持多个 AI 提供商（Kimi、DeepSeek、通义千问、豆包、智谱 GLM、自定义）
- ✨ AI 自然语言快速创建任务（首页 FAB 按钮）
- ☑️ 任务列表批量选择与删除（长按进入选择模式，支持全选）

**修复：**
- 修复 API 双重 Authorization 头导致调用失败的问题
- 修复 Material3 1.2.0 API 兼容性（Divider → HorizontalDivider，ListItem 参数名）
- 修复 Room KSP 编译错误（NULLS LAST 语法、exportSchema、空 Converters）
- 修复 Hilt 依赖注入（TaskMapper 缺少 @Inject constructor）
- 修复 Kotlin 编译错误（JsonParser、dp import、combine 类型推断、@OptIn 注解）

### v1.0.0 (2026-04-23)

**初始版本：**
- 📱 基于 Jetpack Compose + Material 3 的 Android 应用
- 🏗️ MVVM + Clean Architecture 架构
- 🤖 Kimi AI 接入（自然语言任务解析、智能建议）
- 📋 任务管理（增删改查、优先级、分类、状态）
- ⏰ 智能提醒（通知、声音、振动）
- 💧 健康提醒（喝水定时提醒）
- 🎨 深色/浅色/跟随系统主题
- 🔄 GitHub Actions 自动构建

## License

MIT
