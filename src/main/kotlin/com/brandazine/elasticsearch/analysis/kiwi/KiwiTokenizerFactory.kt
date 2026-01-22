package com.brandazine.elasticsearch.analysis.kiwi

import kr.pe.bab2min.Kiwi
import org.apache.lucene.analysis.Tokenizer
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.TokenizerFactory

/**
 * Factory for creating KiwiTokenizer instances.
 *
 * This factory is registered with Elasticsearch via the classic plugin API.
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
class KiwiTokenizerFactory(
    indexSettings: IndexSettings,
    environment: Environment,
    private val name: String,
    settings: Settings
) : TokenizerFactory {

    private val kiwi: Kiwi
    private val discardPunctuation: Boolean
    private val posTagsToInclude: Set<String>?

    init {
        // Parse settings manually from Settings object
        val modelPath = resolvePath(environment, settings.get("model_path", "kiwi"))
        val numThreads = settings.getAsInt("num_threads", 0)
        discardPunctuation = settings.getAsBoolean("discard_punctuation", true)
        val userDict = settings.get("user_dictionary")?.takeIf { it.isNotEmpty() }
            ?.let { resolvePath(environment, it) }
        posTagsToInclude = settings.getAsList("pos_tags_to_include")
            .takeIf { it.isNotEmpty() }?.toSet()

        // Get or create Kiwi instance (shared across tokenizers with same config)
        kiwi = KiwiInstanceManager.getInstance(
            modelPath = modelPath,
            numThreads = numThreads,
            userDictionary = userDict
        )
    }

    companion object {
        /**
         * Resolves a path against the ES config directory if it's relative.
         * Absolute paths are returned unchanged.
         */
        private fun resolvePath(environment: Environment, path: String): String {
            val file = java.io.File(path)
            return if (file.isAbsolute) {
                path
            } else {
                environment.configFile().resolve(path).toAbsolutePath().toString()
            }
        }
    }

    override fun name(): String = name

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
