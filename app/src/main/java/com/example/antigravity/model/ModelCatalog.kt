package com.example.antigravity.model

object ModelCatalog {

    val allModels: List<ModelInfo> = listOf(
        // --- KiloCode Free Models ---
        ModelInfo(
            id = "kilo/deepseek-r1",
            name = "DeepSeek R1 (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Full frontier open reasoning model with chain-of-thought verification served free on KiloCode.",
            tags = listOf("free", "kilocode", "deepseek", "reasoning", "r1", "cot")
        ),
        ModelInfo(
            id = "kilo/deepseek-r1-distill-qwen-32b",
            name = "DeepSeek R1 Distill Qwen 32B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "64k",
            description = "High-efficiency distillation of DeepSeek R1 reasoning architecture provided free on KiloCode gateway.",
            tags = listOf("free", "kilocode", "deepseek", "reasoning", "r1", "cot")
        ),
        ModelInfo(
            id = "kilo/deepseek-r1-distill-llama-70b",
            name = "DeepSeek R1 Distill Llama 70B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Massive 70B parameter reasoning distillation on Llama architecture served free on KiloCode.",
            tags = listOf("free", "kilocode", "deepseek", "llama", "reasoning", "r1")
        ),
        ModelInfo(
            id = "kilo/deepseek-v3",
            name = "DeepSeek V3 671B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "128k",
            description = "State-of-the-art general intelligence 671B MoE architecture available at zero cost on KiloCode.",
            tags = listOf("free", "kilocode", "deepseek", "moe", "v3", "general")
        ),
        ModelInfo(
            id = "kilo/qwen-2.5-coder-32b",
            name = "Qwen 2.5 Coder 32B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "32k",
            description = "Premier code intelligence and autonomous refactoring model served with zero cost on KiloCode.",
            tags = listOf("free", "kilocode", "qwen", "coder", "coding", "programming")
        ),
        ModelInfo(
            id = "kilo/qwen-2.5-coder-14b",
            name = "Qwen 2.5 Coder 14B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "32k",
            description = "Balanced high-performance code assistant on KiloCode gateway.",
            tags = listOf("free", "kilocode", "qwen", "coder", "coding")
        ),
        ModelInfo(
            id = "kilo/qwen-2.5-coder-7b",
            name = "Qwen 2.5 Coder 7B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "32k",
            description = "Ultra-fast low-latency code generation model on KiloCode.",
            tags = listOf("free", "kilocode", "qwen", "coder", "fast")
        ),
        ModelInfo(
            id = "kilo/llama-3.3-70b-instruct",
            name = "Llama 3.3 70B Instruct (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Meta's flagship open-weights model accessible with free API tier on KiloCode.",
            tags = listOf("free", "kilocode", "llama", "meta", "instruct")
        ),
        ModelInfo(
            id = "kilo/llama-3.1-8b-instruct",
            name = "Llama 3.1 8B Instruct (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Swift and agile open model for fast multi-turn completions on KiloCode.",
            tags = listOf("free", "kilocode", "llama", "meta", "fast")
        ),
        ModelInfo(
            id = "kilo/phi-4",
            name = "Microsoft Phi-4 14B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "16k",
            description = "Microsoft's 14B state-of-the-art synthetic reasoning model available for free on KiloCode.",
            tags = listOf("free", "kilocode", "microsoft", "phi", "math", "reasoning")
        ),
        ModelInfo(
            id = "kilo/mistral-small-3",
            name = "Mistral Small 3 24B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "32k",
            description = "European frontier open weights model with premier function calling on KiloCode.",
            tags = listOf("free", "kilocode", "mistral", "fast")
        ),
        ModelInfo(
            id = "kilo/codestral-22b",
            name = "Mistral Codestral 22B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "32k",
            description = "Mistral's dedicated code generation and fill-in-the-middle model on KiloCode.",
            tags = listOf("free", "kilocode", "mistral", "codestral", "coding")
        ),
        ModelInfo(
            id = "kilo/gemma-2-27b",
            name = "Google Gemma 2 27B (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "8k",
            description = "Google DeepMind open model with high benchmark scores on KiloCode free gateway.",
            tags = listOf("free", "kilocode", "google", "gemma")
        ),
        ModelInfo(
            id = "kilo/glm-4-9b",
            name = "GLM-4 9B Chat (KiloCode)",
            gateway = ModelGateway.KILOCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Zhipu AI bilingual reasoning and agent model served free on KiloCode.",
            tags = listOf("free", "kilocode", "glm", "multilingual")
        ),

        // --- OpenCode Free Models ---
        ModelInfo(
            id = "opencode/deepseek-coder-v2-instruct",
            name = "DeepSeek Coder V2 236B (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Flagship 236B MoE software engineering powerhouse matching closed models on OpenCode.",
            tags = listOf("free", "opencode", "deepseek", "coder", "coding", "software", "moe")
        ),
        ModelInfo(
            id = "opencode/deepseek-coder-v2-lite",
            name = "DeepSeek Coder V2 Lite (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "64k",
            description = "Specialized software engineering model supporting 300+ programming languages on OpenCode.",
            tags = listOf("free", "opencode", "deepseek", "coder", "coding", "software")
        ),
        ModelInfo(
            id = "opencode/qwen-2.5-coder-32b",
            name = "Qwen 2.5 Coder 32B (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "32k",
            description = "Leading open programming intelligence model for complex refactors on OpenCode.",
            tags = listOf("free", "opencode", "qwen", "coder", "coding")
        ),
        ModelInfo(
            id = "opencode/qwen-2.5-coder-14b",
            name = "Qwen 2.5 Coder 14B (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "32k",
            description = "Versatile intermediate code generation model on OpenCode gateway.",
            tags = listOf("free", "opencode", "qwen", "coder", "coding")
        ),
        ModelInfo(
            id = "opencode/qwen-2.5-coder-7b",
            name = "Qwen 2.5 Coder 7B (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "32k",
            description = "Fast, lightweight coding model ideal for rapid iteration and code completion on OpenCode.",
            tags = listOf("free", "opencode", "qwen", "coder", "fast", "low-latency")
        ),
        ModelInfo(
            id = "opencode/starcoder2-15b",
            name = "StarCoder2 15B (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "16k",
            description = "BigCode alliance model trained on 600+ languages from GitHub on OpenCode gateway.",
            tags = listOf("free", "opencode", "starcoder", "bigcode", "github", "coding")
        ),
        ModelInfo(
            id = "opencode/starcoder2-7b",
            name = "StarCoder2 7B (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "16k",
            description = "Efficient code syntax and snippet synthesis engine on OpenCode.",
            tags = listOf("free", "opencode", "starcoder", "coding")
        ),
        ModelInfo(
            id = "opencode/codellama-70b-instruct",
            name = "CodeLlama 70B Instruct (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "100k",
            description = "Meta's highest parameter specialized code model available on OpenCode gateway.",
            tags = listOf("free", "opencode", "codellama", "meta", "coding")
        ),
        ModelInfo(
            id = "opencode/codellama-34b-instruct",
            name = "CodeLlama 34B Instruct (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "100k",
            description = "Fast Python and multi-language syntax specialist on OpenCode.",
            tags = listOf("free", "opencode", "codellama", "coding")
        ),
        ModelInfo(
            id = "opencode/glm-4-flash-free",
            name = "GLM-4 Flash (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "128k",
            description = "High-speed multi-lingual and code instruction model served free on OpenCode gateway.",
            tags = listOf("free", "opencode", "glm", "flash", "reasoning", "multilingual")
        ),
        ModelInfo(
            id = "opencode/wizardcoder-python-34b",
            name = "WizardCoder Python 34B (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "32k",
            description = "Complex algorithmic problem solving and unit test generator on OpenCode.",
            tags = listOf("free", "opencode", "wizardcoder", "python", "coding")
        ),
        ModelInfo(
            id = "opencode/devvinci-code-instruct",
            name = "DevVinci Code Specialist (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "64k",
            description = "Specialized multi-file architectural refactoring model on OpenCode.",
            tags = listOf("free", "opencode", "devvinci", "architecture", "coding")
        ),
        ModelInfo(
            id = "opencode/zen-bigpickle",
            name = "Zen BigPickle Internal (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Zen internal experimental foundation model with extremely advanced coding capabilities.",
            tags = listOf("free", "opencode", "zen", "bigpickle", "experimental")
        ),
        ModelInfo(
            id = "opencode/zen-coder-internal",
            name = "Zen Coder Internal (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Zen internal high-velocity coding and architecture synthesis foundation model.",
            tags = listOf("free", "opencode", "zen", "coder", "coding")
        ),
        ModelInfo(
            id = "opencode/zen-multimodal-internal",
            name = "Zen Vision Multimodal (OpenCode)",
            gateway = ModelGateway.OPENCODE,
            isFree = true,
            contextWindow = "128k",
            description = "Zen internal vision-language multimodal reasoning foundation model.",
            tags = listOf("free", "opencode", "zen", "multimodal", "vision")
        ),

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
            id = "gemini-2.0-flash",
            name = "Gemini 2.0 Flash",
            gateway = ModelGateway.GEMINI,
            isFree = true,
            contextWindow = "1M",
            description = "Google's recommended agentic model. Free tier included with fast multimodal reasoning.",
            tags = listOf("free", "google", "gemini", "flash", "agentic", "multimodal", "fast")
        ),
        ModelInfo(
            id = "gemini-1.5-flash",
            name = "Gemini 1.5 Flash",
            gateway = ModelGateway.GEMINI,
            isFree = true,
            contextWindow = "1M",
            description = "Ultra-stable, highly efficient workhorse model supported across all Google AI Studio tiers.",
            tags = listOf("free", "google", "gemini", "flash", "stable", "multimodal")
        ),
        ModelInfo(
            id = "gemini-1.5-pro",
            name = "Gemini 1.5 Pro",
            gateway = ModelGateway.GEMINI,
            isFree = false,
            contextWindow = "2M",
            description = "Google's premier model for complex multi-file coding and advanced tool reasoning.",
            tags = listOf("google", "gemini", "pro", "coding", "complex")
        ),
        ModelInfo(
            id = "gemini-2.5-flash",
            name = "Gemini 2.5 Flash (Preview)",
            gateway = ModelGateway.GEMINI,
            isFree = true,
            contextWindow = "1M",
            description = "Preview hybrid reasoning model with dynamic thinking budgets.",
            tags = listOf("free", "google", "gemini", "flash", "preview")
        ),
        ModelInfo(
            id = "gemini-2.5-pro",
            name = "Gemini 2.5 Pro (Preview)",
            gateway = ModelGateway.GEMINI,
            isFree = false,
            contextWindow = "2M",
            description = "Preview deep coding reasoning model in the 2.5 family.",
            tags = listOf("google", "gemini", "pro", "preview")
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

        // --- OpenAI Models ---
        ModelInfo(
            id = "gpt-4o",
            name = "GPT-4o (Omni)",
            gateway = ModelGateway.OPENAI,
            isFree = false,
            contextWindow = "128k",
            description = "OpenAI's flagship multimodal model for high-intelligence coding and reasoning.",
            tags = listOf("openai", "gpt-4o", "flagship", "multimodal", "coding")
        ),
        ModelInfo(
            id = "gpt-4o-mini",
            name = "GPT-4o Mini",
            gateway = ModelGateway.OPENAI,
            isFree = false,
            contextWindow = "128k",
            description = "Fast, cost-efficient small model for everyday coding and agent tasks.",
            tags = listOf("openai", "gpt-4o-mini", "fast", "affordable")
        ),
        ModelInfo(
            id = "o3-mini",
            name = "OpenAI o3-mini",
            gateway = ModelGateway.OPENAI,
            isFree = false,
            contextWindow = "200k",
            description = "OpenAI's latest cost-efficient reasoning model optimized for STEM, coding, and math.",
            tags = listOf("openai", "o3-mini", "reasoning", "stem", "coding")
        ),
        ModelInfo(
            id = "o1-mini",
            name = "OpenAI o1-mini",
            gateway = ModelGateway.OPENAI,
            isFree = false,
            contextWindow = "128k",
            description = "Reasoning model designed for complex multi-step programming challenges.",
            tags = listOf("openai", "o1-mini", "reasoning", "programming")
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

    fun mergeModels(liveModels: List<ModelInfo>): List<ModelInfo> {
        val result = allModels.toMutableList()
        for (live in liveModels) {
            val existingIndex = result.indexOfFirst { 
                it.id.equals(live.id, ignoreCase = true) && 
                    (it.gateway == live.gateway && (it.gateway != ModelGateway.CUSTOM || it.providerName.equals(live.providerName, ignoreCase = true)))
            }
            if (existingIndex >= 0) {
                val existing = result[existingIndex]
                val mergedTags = (existing.tags + live.tags).distinct()
                result[existingIndex] = existing.copy(
                    name = if (existing.name.isBlank() || existing.name == existing.id) live.name else existing.name,
                    tags = mergedTags,
                    contextWindow = if (live.contextWindow.isNotBlank() && live.contextWindow != "128k") live.contextWindow else existing.contextWindow,
                    providerName = if (live.providerName.isNotBlank()) live.providerName else existing.providerName,
                    isFree = existing.isFree || live.isFree
                )
            } else {
                result.add(live)
            }
        }
        return result
    }

    fun findModel(id: String, customList: List<ModelInfo>? = null): ModelInfo? {
        val pool = customList ?: allModels
        return pool.find { 
            it.id.equals(id, ignoreCase = true) || 
            it.name.equals(id, ignoreCase = true)
        } ?: allModels.find { 
            it.id.equals(id, ignoreCase = true) || 
            it.name.equals(id, ignoreCase = true)
        }
    }
}
