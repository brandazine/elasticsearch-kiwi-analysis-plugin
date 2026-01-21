package com.brandazine.elasticsearch.analysis.kiwi

import org.apache.lucene.analysis.TokenStream
import org.elasticsearch.plugin.Inject
import org.elasticsearch.plugin.NamedComponent
import org.elasticsearch.plugin.analysis.TokenFilterFactory

/**
 * Factory for creating KiwiPartOfSpeechFilter instances.
 *
 * This factory is registered with Elasticsearch via the @NamedComponent annotation.
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
@NamedComponent("kiwi_part_of_speech")
class KiwiPartOfSpeechFilterFactory @Inject constructor(
    private val settings: KiwiPartOfSpeechSettings
) : TokenFilterFactory {

    private val stopTags: Set<String>

    init {
        val configuredTags = settings.stopTags()
        stopTags = if (configuredTags.isNotEmpty()) {
            configuredTags.toSet()
        } else {
            POSTagSet.DEFAULT_STOP_TAGS
        }
    }

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
