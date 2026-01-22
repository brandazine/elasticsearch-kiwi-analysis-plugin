package com.brandazine.elasticsearch.analysis.kiwi

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.AnalyzerProvider
import org.elasticsearch.index.analysis.AnalyzerScope

/**
 * Provider for creating KiwiAnalyzer instances.
 *
 * This provider is registered with Elasticsearch via the classic plugin API.
 * When an index configuration specifies `"type": "kiwi"` for an analyzer,
 * Elasticsearch will use this provider to create analyzer instances.
 *
 * The KiwiAnalyzer combines:
 * - Kiwi tokenizer for morphological analysis
 * - POS filter for removing particles/endings (configurable)
 * - Lowercase filter
 *
 * Example index settings:
 * ```json
 * {
 *   "settings": {
 *     "analysis": {
 *       "analyzer": {
 *         "korean_analyzer": {
 *           "type": "kiwi",
 *           "model_path": "/etc/elasticsearch/kiwi",
 *           "discard_punctuation": true,
 *           "stop_tags": ["JKS", "JKO", "JX", "EP", "EF", "EC"]
 *         }
 *       }
 *     }
 *   }
 * }
 * ```
 */
class KiwiAnalyzerProvider(
    indexSettings: IndexSettings,
    environment: Environment,
    private val name: String,
    settings: Settings
) : AnalyzerProvider<KiwiAnalyzer> {

    private val analyzer: KiwiAnalyzer

    init {
        // Parse settings manually from Settings object
        val modelPath = settings.get("model_path", "kiwi")
        val numThreads = settings.getAsInt("num_threads", 0)
        val discardPunctuation = settings.getAsBoolean("discard_punctuation", true)
        val userDict = settings.get("user_dictionary")?.takeIf { it.isNotEmpty() }

        val kiwi = KiwiInstanceManager.getInstance(
            modelPath = modelPath,
            numThreads = numThreads,
            userDictionary = userDict
        )

        val stopTags = settings.getAsList("stop_tags")
            .takeIf { it.isNotEmpty() }?.toSet()
            ?: POSTagSet.DEFAULT_STOP_TAGS

        analyzer = KiwiAnalyzer(
            kiwi = kiwi,
            discardPunctuation = discardPunctuation,
            posTagsToInclude = null,
            stopTags = stopTags,
            applyPosFilter = true,
            applyLowercase = true
        )
    }

    override fun name(): String = name

    override fun scope(): AnalyzerScope = AnalyzerScope.INDEX

    /**
     * Get the analyzer instance.
     *
     * Returns the pre-configured KiwiAnalyzer instance.
     * Analyzers are thread-safe and can be shared.
     */
    override fun get(): KiwiAnalyzer {
        return analyzer
    }
}
