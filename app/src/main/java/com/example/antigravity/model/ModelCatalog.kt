package com.example.antigravity.model

object ModelCatalog {

    private val _allModels = java.util.Collections.synchronizedList(mutableListOf<ModelInfo>())
    val allModels: List<ModelInfo> get() = _allModels.toList()

    fun mergeModels(liveModels: List<ModelInfo>): List<ModelInfo> {
        val result = _allModels.toMutableList()
        for (live in liveModels) {
            val existingIndex = result.indexOfFirst {
                it.id.equals(live.id, ignoreCase = true) &&
                    (it.gateway == live.gateway && (it.gateway != ModelGateway.CUSTOM || it.providerName.equals(live.providerName, ignoreCase = true)))
            }
            if (existingIndex >= 0) {
                val existing = result[existingIndex]
                val mergedTags = (existing.tags + live.tags).distinct()
                result[existingIndex] = existing.copy(
                    name = if (live.name.isNotBlank()) live.name else existing.name,
                    tags = mergedTags,
                    contextWindow = if (live.contextWindow.isNotBlank() && live.contextWindow != "128k") live.contextWindow else existing.contextWindow,
                    providerName = if (live.providerName.isNotBlank()) live.providerName else existing.providerName,
                    isFree = existing.isFree || live.isFree
                )
            } else {
                result.add(live)
            }
        }
        _allModels.clear()
        _allModels.addAll(result)
        return result
    }

    fun setModels(models: List<ModelInfo>) {
        _allModels.clear()
        _allModels.addAll(models)
    }

    fun findModel(id: String, customList: List<ModelInfo>? = null): ModelInfo? {
        val pool = customList ?: allModels
        return pool.find {
            it.id.equals(id, ignoreCase = true) ||
            it.name.equals(id, ignoreCase = true)
        } ?: pool.find {
            it.id.endsWith("/$id", ignoreCase = true) || it.id.endsWith(id, ignoreCase = true)
        }
    }

    fun firstForGateway(gateway: ModelGateway): ModelInfo? {
        return allModels.firstOrNull { it.gateway == gateway }
    }
}
