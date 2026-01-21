package com.brandazine.elasticsearch.analysis.kiwi

import kr.pe.bab2min.Kiwi
import org.apache.lucene.analysis.Analyzer
import org.elasticsearch.plugin.Inject
import org.elasticsearch.plugin.NamedComponent
import org.elasticsearch.plugin.analysis.AnalyzerFactory

/**
 * Factory for creating KiwiAnalyzer instances.
 *
 * This factory is registered with Elasticsearch via the @NamedComponent annotation.
 * When an index configuration specifies `"type": "kiwi"` for an analyzer,
 * Elasticsearch will use this factory to create analyzer instances.
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
@NamedComponent("kiwi")
class KiwiAnalyzerFactory @Inject constructor(
    private val settings: KiwiAnalyzerSettings
) : AnalyzerFactory {

    private val analyzer: KiwiAnalyzer

    init {
        val userDict = settings.userDictionary().takeIf { it.isNotEmpty() }

        val kiwi = KiwiInstanceManager.getInstance(
            modelPath = settings.modelPath(),
            numThreads = settings.numThreads(),
            userDictionary = userDict
        )

        val stopTags = settings.stopTags().takeIf { it.isNotEmpty() }?.toSet()
            ?: POSTagSet.DEFAULT_STOP_TAGS

        analyzer = KiwiAnalyzer(
            kiwi = kiwi,
            discardPunctuation = settings.discardPunctuation(),
            posTagsToInclude = null,
            stopTags = stopTags,
            applyPosFilter = true,
            applyLowercase = true
        )
    }

    /**
     * Get the analyzer instance.
     *
     * Returns the pre-configured KiwiAnalyzer instance.
     * Analyzers are thread-safe and can be shared.
     */
    override fun create(): Analyzer {
        return analyzer
    }
}
