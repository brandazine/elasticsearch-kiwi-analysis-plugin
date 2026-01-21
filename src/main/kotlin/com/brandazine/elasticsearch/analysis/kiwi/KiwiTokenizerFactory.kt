package com.brandazine.elasticsearch.analysis.kiwi

import kr.pe.bab2min.Kiwi
import org.apache.lucene.analysis.Tokenizer
import org.elasticsearch.plugin.Inject
import org.elasticsearch.plugin.NamedComponent
import org.elasticsearch.plugin.analysis.TokenizerFactory

/**
 * Factory for creating KiwiTokenizer instances.
 *
 * This factory is registered with Elasticsearch via the @NamedComponent annotation.
 * When an index configuration specifies `"type": "kiwi"` for a tokenizer,
 * Elasticsearch will use this factory to create tokenizer instances.
 *
 * Example index settings:
 * ```json
 * {
 *   "settings": {
 *     "analysis": {
 *       "tokenizer": {
 *         "my_kiwi_tokenizer": {
 *           "type": "kiwi",
 *           "model_path": "/etc/elasticsearch/kiwi",
 *           "discard_punctuation": true,
 *           "user_dictionary": "/etc/elasticsearch/kiwi/user_dict.txt"
 *         }
 *       }
 *     }
 *   }
 * }
 * ```
 */
@NamedComponent("kiwi")
class KiwiTokenizerFactory @Inject constructor(
    private val settings: KiwiTokenizerSettings
) : TokenizerFactory {

    private val kiwi: Kiwi
    private val discardPunctuation: Boolean
    private val posTagsToInclude: Set<String>?

    init {
        // Get or create Kiwi instance (shared across tokenizers with same config)
        val userDict = settings.userDictionary().takeIf { it.isNotEmpty() }

        kiwi = KiwiInstanceManager.getInstance(
            modelPath = settings.modelPath(),
            numThreads = settings.numThreads(),
            userDictionary = userDict
        )

        discardPunctuation = settings.discardPunctuation()
        posTagsToInclude = settings.posTagsToInclude().takeIf { it.isNotEmpty() }?.toSet()
    }

    /**
     * Create a new KiwiTokenizer instance.
     *
     * This is called for each analysis operation (indexing or search).
     * The Kiwi instance is shared, but each Tokenizer has its own state.
     */
    override fun create(): Tokenizer {
        return KiwiTokenizer(
            kiwi = kiwi,
            discardPunctuation = discardPunctuation,
            posTagsToInclude = posTagsToInclude
        )
    }
}
