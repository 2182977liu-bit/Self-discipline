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

## License

MIT
