package com.example.antigravity.model

object ModelCatalog {

    val allModels: List<ModelInfo> = listOf(
        // --- OpenRouter Free Models ---
        ModelInfo(
            id = "meta-llama/llama-3.3-70b-instruct:free",
            name = "Llama 3.3 70B Instruct",
            gateway = ModelGateway.OPENROUTER,
            isFree = true,
            contextWindow = "131k",
            description = "Meta's flagship open model. Exceptional general intelligence, code generation, and complex reasoning.",
            tags = listOf("free", "meta", "llama", "coding", "reasoning", "openrouter")
        ),
        ModelInfo(
            id = "google/gemini-2.0-flash-exp:free",
            name = "Gemini 2.0 Flash Exp",
            gateway = ModelGateway.OPENROUTER,
            isFree = true,
            contextWindow = "1M",
            description = "Google's experimental next-gen multimodal model with native tool use and blazing fast responses.",
            tags = listOf("free", "google", "gemini", "multimodal", "fast", "openrouter")
        ),
        ModelInfo(
            id = "deepseek/deepseek-r1:free",
            name = "DeepSeek R1",
            gateway = ModelGateway.OPENROUTER,
            isFree = true,
            contextWindow = "164k",
            description = "State-of-the-art open reasoning model utilizing reinforcement learning for complex logic and math.",
            tags = listOf("free", "deepseek", "reasoning", "math", "cot", "openrouter")
        ),
        ModelInfo(
            id = "qwen/qwen-2.5-coder-32b-instruct:free",
            name = "Qwen 2.5 Coder 32B",
            gateway = ModelGateway.OPENROUTER,
            isFree = true,
            contextWindow = "32k",
            description = "Alibaba's dedicated code model, competitive with closed source models on programming benchmarks.",
            tags = listOf("free", "qwen", "coder", "coding", "programming", "openrouter")
        ),
        ModelInfo(
            id = "mistralai/mistral-7b-instruct:free",
            name = "Mistral 7B Instruct",
            gateway = ModelGateway.OPENROUTER,
            isFree = true,
            contextWindow = "32k",
            description = "Compact, high-throughput model ideal for agentic loops and quick refactoring tasks.",
            tags = listOf("free", "mistral", "fast", "lightweight", "openrouter")
        ),
        ModelInfo(
            id = "microsoft/phi-3-mini-128k-instruct:free",
            name = "Phi-3 Mini 128K",
            gateway = ModelGateway.OPENROUTER,
            isFree = true,
            contextWindow = "128k",
            description = "Microsoft's efficient small language model trained on textbook-quality data.",
            tags = listOf("free", "microsoft", "phi", "compact", "openrouter")
        ),

        // --- Groq Free Tier Models ---
        ModelInfo(
            id = "llama-3.3-70b-versatile",
            name = "Llama 3.3 70B Versatile (Groq)",
            gateway = ModelGateway.GROQ,
            isFree = true,
            contextWindow = "128k",
            description = "Ultra-fast LPU inference at 300+ tokens/sec on Groq free tier.",
            tags = listOf("free", "groq", "llama", "fast", "lpu")
        ),
        ModelInfo(
            id = "llama-3.1-8b-instant",
            name = "Llama 3.1 8B Instant (Groq)",
            gateway = ModelGateway.GROQ,
            isFree = true,
            contextWindow = "128k",
            description = "Blazing sub-second latency at 800+ tokens/sec on Groq free tier.",
            tags = listOf("free", "groq", "llama", "instant", "ultra-fast")
        ),
        ModelInfo(
            id = "mixtral-8x7b-32768",
            name = "Mixtral 8x7B MoE (Groq)",
            gateway = ModelGateway.GROQ,
            isFree = true,
            contextWindow = "32k",
            description = "Mixture of Experts architecture on Groq LPU with large 32k context.",
            tags = listOf("free", "groq", "mixtral", "moe")
        ),
        ModelInfo(
            id = "gemma2-9b-it",
            name = "Gemma 2 9B IT (Groq)",
            gateway = ModelGateway.GROQ,
            isFree = true,
            contextWindow = "8k",
            description = "Google's lightweight open weights model running on Groq hardware.",
            tags = listOf("free", "groq", "gemma", "google")
        ),

        // --- Google Gemini Models ---
        ModelInfo(
            id = "gemini-2.5-flash",
            name = "Gemini 2.5 Flash",
            gateway = ModelGateway.GEMINI,
            isFree = true,
            contextWindow = "1M",
            description = "Google's recommended agentic model. Free tier included with fast multimodal reasoning.",
            tags = listOf("free", "google", "gemini", "flash", "agentic", "multimodal")
        ),
        ModelInfo(
            id = "gemini-2.5-flash-lite",
            name = "Gemini 2.5 Flash-Lite",
            gateway = ModelGateway.GEMINI,
            isFree = true,
            contextWindow = "1M",
            description = "Optimized for extreme efficiency and cost-free high throughput agent tasks.",
            tags = listOf("free", "google", "gemini", "lite", "high-throughput")
        ),
        ModelInfo(
            id = "gemini-2.5-pro",
            name = "Gemini 2.5 Pro",
            gateway = ModelGateway.GEMINI,
            isFree = false,
            contextWindow = "2M",
            description = "Google's most capable model for complex multi-file coding and advanced tool reasoning.",
            tags = listOf("google", "gemini", "pro", "coding", "complex")
        ),
        ModelInfo(
            id = "gemma-4",
            name = "Gemma 4 Open Foundation",
            gateway = ModelGateway.GEMINI,
            isFree = true,
            contextWindow = "128k",
            description = "Google DeepMind's open weights model with state-of-the-art developer performance.",
            tags = listOf("free", "google", "gemma", "open-weights")
        ),

        // --- Ollama / Local (100% Free & Private) ---
        ModelInfo(
            id = "llama3.3:latest",
            name = "Llama 3.3 (Ollama Local)",
            gateway = ModelGateway.OLLAMA,
            isFree = true,
            contextWindow = "128k",
            description = "100% private, offline model running on your local machine or local network via Ollama.",
            tags = listOf("free", "local", "ollama", "offline", "private", "llama")
        ),
        ModelInfo(
            id = "qwen2.5-coder:latest",
            name = "Qwen 2.5 Coder (Ollama Local)",
            gateway = ModelGateway.OLLAMA,
            isFree = true,
            contextWindow = "32k",
            description = "Local programming specialist running offline on Ollama with full privacy.",
            tags = listOf("free", "local", "ollama", "offline", "coding", "qwen")
        ),
        ModelInfo(
            id = "deepseek-r1:8b",
            name = "DeepSeek R1 8B (Ollama Local)",
            gateway = ModelGateway.OLLAMA,
            isFree = true,
            contextWindow = "64k",
            description = "Local reasoning model capable of mathematical proof and step-by-step thinking offline.",
            tags = listOf("free", "local", "ollama", "offline", "reasoning", "deepseek")
        ),

        // --- Hugging Face Models ---
        ModelInfo(
            id = "meta-llama/Llama-3.2-3B-Instruct",
            name = "Llama 3.2 3B (Hugging Face)",
            gateway = ModelGateway.HUGGINGFACE,
            isFree = true,
            contextWindow = "128k",
            description = "Lightweight on-device optimized model via Hugging Face Serverless Inference API.",
            tags = listOf("free", "huggingface", "llama", "mobile")
        ),
        ModelInfo(
            id = "Qwen/Qwen2.5-7B-Instruct",
            name = "Qwen 2.5 7B (Hugging Face)",
            gateway = ModelGateway.HUGGINGFACE,
            isFree = true,
            contextWindow = "32k",
            description = "Powerful instruction-tuned general model available on Hugging Face Inference API.",
            tags = listOf("free", "huggingface", "qwen", "instruct")
        )
    )

    fun findModel(id: String): ModelInfo? {
        return allModels.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
    }
}
