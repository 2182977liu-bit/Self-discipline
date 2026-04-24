# Self-discipline

AI 智能生活管家 — 输入目标，AI 自动规划你的一天

## 功能特性

### 🤖 AI 生活管家（核心功能）
- **智能目标规划** — 输入你的目标（如"未来一周学习C++，早睡早起，轻度运动"），AI 根据复杂度自动生成计划
- **天气感知** — 自动获取当前位置天气，雨天安排室内运动，晴天推荐户外活动
- **闹钟 + 通知提醒** — 每个计划项自动设置闹钟，到点弹窗通知
- **打卡追踪** — 入睡🌙 / 醒来☀️ / 运动🏃 / 吃饭🍽️ / 学习📖 一键打卡
- **步数统计** — 实时显示今日步数（传感器低功耗检测）

### 🤖 多 AI 提供商
- 支持 Kimi、DeepSeek、通义千问、豆包、智谱 GLM、自定义（OpenAI 兼容）
- 在设置中一键切换，所有 AI 功能自动适配

### 🎨 界面
- Material Design 3 (Material You) 风格
- 深色/浅色/跟随系统主题
- 精简设计：首页 + 设置，两个 Tab 搞定一切

## 使用方法

### 1. 配置 AI（首次使用）

打开 APP → 底部 **设置** → **AI 设置** → 选择提供商 → 输入 API 密钥

| 提供商 | 获取密钥 |
|--------|---------|
| Kimi (月之暗面) | [platform.moonshot.cn](https://platform.moonshot.cn/) |
| DeepSeek (深度求索) | [platform.deepseek.com](https://platform.deepseek.com/) |
| 通义千问 (阿里) | [dashscope.console.aliyun.com](https://dashscope.console.aliyun.com/) |
| 豆包 (字节跳动) | [console.volcengine.com/ark](https://console.volcengine.com/ark) |
| 智谱 GLM | [open.bigmodel.cn](https://open.bigmodel.cn/) |

### 2. 设定目标

点击右下角 ✨ 按钮 → 输入你的目标 → 点击"生成计划"

**简单目标示例：**
- "提醒我每2小时喝一次水" → AI 只生成 2-3 个喝水提醒
- "今晚10点提醒我睡觉" → AI 只生成 1 个提醒

**复杂目标示例：**
- "未来一周学习C++，早睡早起，轻度运动改善健康" → AI 生成完整的一天计划

### 3. 打卡与追踪

- 首页快捷打卡按钮：入睡 / 醒来 / 运动 / 吃饭 / 学习
- 打卡后自动发送通知确认
- 今日步数实时显示

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| 依赖注入 | Hilt |
| 数据库 | Room (KSP) |
| 网络 | Retrofit + OkHttp + Gson |
| 天气 API | Open-Meteo（免费无需 key） |
| 传感器 | Step Counter（步数）、UsageStats（屏幕使用） |
| AI 接口 | OpenAI 兼容 `/v1/chat/completions` |
| CI/CD | GitHub Actions |

## 项目结构

```
app/src/main/java/com/example/timemanager/
├── data/
│   ├── local/               # 本地存储 (Room, DataStore)
│   ├── remote/              # 网络请求 (AI API, 天气 API)
│   └── repository/          # Repository 实现
├── domain/
│   ├── model/               # 领域模型 (Goal, DailyPlan, CheckIn, AIProvider...)
│   ├── repository/          # Repository 接口
│   └── usecase/             # 用例
├── presentation/
│   ├── screen/              # 页面 (首页, 设置)
│   ├── navigation/          # 导航
│   └── theme/               # 主题
├── service/
│   ├── alarm/               # 闹钟调度
│   ├── notification/        # 通知管理
│   ├── tracking/            # 行为追踪 (步数, 屏幕使用)
│   └── worker/              # 后台任务
└── di/                      # Hilt 依赖注入
```

---

## 📦 如何生成 APK 安装包

### 方式一：从 GitHub Actions 直接下载（推荐）

1. 打开 [Actions](https://github.com/2182977liu-bit/Self-discipline/actions) 标签
2. 点击最新的 **Build Android APK** 工作流
3. 滚动到底部 **Artifacts** → 下载 `app-debug.apk`
4. 传到手机安装（需开启"允许安装未知来源应用"）

### 方式二：Android Studio 构建

1. 克隆项目：`git clone https://github.com/2182977liu-bit/Self-discipline.git`
2. 用 Android Studio 打开项目
3. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**

### 方式三：命令行构建

```bash
git clone https://github.com/2182977liu-bit/Self-discipline.git
cd Self-discipline
./gradlew assembleDebug
# 输出: app/build/outputs/apk/debug/app-debug.apk
```

---

## 📝 更新日志

### v2.0.0 (2026-04-24)

**重大更新 — 从任务管理器升级为 AI 生活管家：**
- 🎯 AI 智能目标规划（根据目标复杂度自动决定计划范围）
- 🌤️ 天气感知（Open-Meteo 免费 API，自动获取天气调整运动安排）
- ⏰ 闹钟 + 通知提醒（计划项自动设置闹钟，到点弹窗通知）
- ✅ 打卡追踪（入睡/醒来/运动/吃饭/学习一键打卡）
- 🚶 步数统计（传感器低功耗检测）
- 📱 屏幕使用检测（辅助判断睡眠，低功耗）
- 🔔 打卡通知确认
- 🔄 重新生成 / 清除计划按钮
- 📐 精简导航（首页 + 设置，两个 Tab）

**修复：**
- 修复简单目标也生成整天计划的问题（重写 AI 提示词）
- 修复步数永远显示 0（接入 StepTracker）
- 修复 Hilt 注入 Context 失败（添加 @ApplicationContext）

### v1.0.1 (2026-04-23)

- 🤖 支持多个 AI 提供商（Kimi、DeepSeek、通义千问、豆包、智谱 GLM、自定义）
- ✨ AI 自然语言快速创建任务
- ☑️ 任务列表批量选择与删除
- 修复 API 双重 Authorization 头、Material3 兼容性、Room KSP 编译等问题

### v1.0.0 (2026-04-23)

- 初始版本：基于 Jetpack Compose + Material 3 的任务管理应用
- Kimi AI 接入、任务 CRUD、智能提醒、GitHub Actions 自动构建

## License

MIT
