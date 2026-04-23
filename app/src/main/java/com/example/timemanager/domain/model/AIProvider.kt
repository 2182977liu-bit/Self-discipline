package com.example.timemanager.domain.model

/**
 * AI 提供商枚举
 *
 * 支持国内主流 AI API，均兼容 OpenAI /v1/chat/completions 格式
 */
enum class AIProvider(
    val key: String,
    val displayName: String,
    val baseUrl: String,
    val defaultModel: String,
    val keyPrefix: String,
    val keyHint: String,
    val websiteUrl: String
) {
    KIMI(
        key = "kimi",
        displayName = "Kimi (月之暗面)",
        baseUrl = "https://api.moonshot.cn/",
        defaultModel = "moonshot-v1-8k",
        keyPrefix = "",
        keyHint = "在 platform.moonshot.cn 获取API密钥",
        websiteUrl = "https://platform.moonshot.cn/"
    ),
    DEEPSEEK(
        key = "deepseek",
        displayName = "DeepSeek (深度求索)",
        baseUrl = "https://api.deepseek.com/",
        defaultModel = "deepseek-chat",
        keyPrefix = "sk-",
        keyHint = "在 platform.deepseek.com 获取API密钥",
        websiteUrl = "https://platform.deepseek.com/"
    ),
    QWEN(
        key = "qwen",
        displayName = "通义千问 (阿里)",
        baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/",
        defaultModel = "qwen-turbo",
        keyPrefix = "sk-",
        keyHint = "在 dashscope.console.aliyun.com 获取API密钥",
        websiteUrl = "https://dashscope.console.aliyun.com/"
    ),
    DOUBAO(
        key = "doubao",
        displayName = "豆包 (字节跳动)",
        baseUrl = "https://ark.cn-beijing.volces.com/api/v3/",
        defaultModel = "doubao-pro-32k",
        keyPrefix = "",
        keyHint = "在 console.volcengine.com/ark 获取API密钥",
        websiteUrl = "https://console.volcengine.com/ark"
    ),
    GLM(
        key = "glm",
        displayName = "智谱 GLM",
        baseUrl = "https://open.bigmodel.cn/api/paas/",
        defaultModel = "glm-4-flash",
        keyPrefix = "",
        keyHint = "在 open.bigmodel.cn 获取API密钥",
        websiteUrl = "https://open.bigmodel.cn/"
    ),
    CUSTOM(
        key = "custom",
        displayName = "自定义 (OpenAI兼容)",
        baseUrl = "",
        defaultModel = "",
        keyPrefix = "",
        keyHint = "填写自定义的API Base URL和模型名称",
        websiteUrl = ""
    );

    companion object {
        fun fromKey(key: String): AIProvider {
            return entries.find { it.key == key } ?: KIMI
        }
    }
}
