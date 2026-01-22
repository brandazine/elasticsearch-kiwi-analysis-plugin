package com.brandazine.elasticsearch.analysis.kiwi

import org.apache.lucene.analysis.TokenStream
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.TokenFilterFactory

/**
 * Factory for creating KiwiPartOfSpeechFilter instances.
 *
 * This factory is registered with Elasticsearch via the classic plugin API.
 * When an index configuration specifies `"type": "kiwi_part_of_speech"` for a filter,
 * Elasticsearch will use this factory to create filter instances.
 *
 * Example index settings:
 * ```json
 * {
 *   "settings": {
 *     "analysis": {
 *       "filter": {
 *         "korean_pos_filter": {
 *           "type": "kiwi_part_of_speech",
 *           "stop_tags": ["JKS", "JKO", "JX", "EP", "EF", "EC"]
 *         }
 *       },
 *       "analyzer": {
 *         "korean": {
 *           "type": "custom",
 *           "tokenizer": "kiwi",
 *           "filter": ["korean_pos_filter", "lowercase"]
 *         }
 *       }
 *     }
 *   }
 * }
 * ```
 */
class KiwiPartOfSpeechFilterFactory(
    indexSettings: IndexSettings,
    environment: Environment,
    private val name: String,
    settings: Settings
) : TokenFilterFactory {

    private val stopTags: Set<String>

    init {
        val configuredTags = settings.getAsList("stop_tags")
        stopTags = if (configuredTags.isNotEmpty()) {
            configuredTags.toSet()
        } else {
            POSTagSet.DEFAULT_STOP_TAGS
        }
    }

    override fun name(): String = name

    /**
     * Create a new KiwiPartOfSpeechFilter instance.
     *
     * @param tokenStream The input token stream to filter
     * @return A new filter wrapping the input stream
     */
    override fun create(tokenStream: TokenStream): TokenStream {
        return KiwiPartOfSpeechFilter(tokenStream, stopTags)
    }
}
