package com.brandazine.elasticsearch.analysis.kiwi

import org.elasticsearch.plugin.settings.AnalysisSettings
import org.elasticsearch.plugin.settings.BooleanSetting
import org.elasticsearch.plugin.settings.IntSetting
import org.elasticsearch.plugin.settings.ListSetting
import org.elasticsearch.plugin.settings.StringSetting

/**
 * Configuration settings for the Kiwi tokenizer.
 *
 * This interface is annotated with @AnalysisSettings, which tells Elasticsearch
 * to create a dynamic proxy that maps JSON configuration to these methods.
 *
 * Example configuration:
 * ```json
 * {
 *   "tokenizer": {
 *     "my_kiwi": {
 *       "type": "kiwi",
 *       "model_path": "/etc/elasticsearch/kiwi",
 *       "num_threads": 2,
 *       "discard_punctuation": true,
 *       "user_dictionary": "/etc/elasticsearch/kiwi/user_dict.txt"
 *     }
 *   }
 * }
 * ```
 */
@AnalysisSettings
interface KiwiTokenizerSettings {

    /**
     * Path to the Kiwi model directory.
     * Default: "kiwi" (relative to ES config directory)
     */
    @StringSetting(path = "model_path", defaultValue = "kiwi")
    fun modelPath(): String

    /**
     * Number of worker threads for tokenization.
     * 0 = auto-detect based on CPU cores.
     * Default: 0
     */
    @IntSetting(path = "num_threads", defaultValue = 0)
    fun numThreads(): Int

    /**
     * Whether to discard punctuation tokens.
     * Default: true
     */
    @BooleanSetting(path = "discard_punctuation", defaultValue = true)
    fun discardPunctuation(): Boolean

    /**
     * Path to user dictionary file.
     * File format: word<TAB>POS_tag<TAB>score (one per line)
     * Default: "" (no user dictionary)
     */
    @StringSetting(path = "user_dictionary", defaultValue = "")
    fun userDictionary(): String

    /**
     * List of POS tags to include in output.
     * If empty, all tokens are included.
     * Default: empty (include all)
     */
    @ListSetting(path = "pos_tags_to_include")
    fun posTagsToInclude(): List<String>
}

/**
 * Configuration settings for the Kiwi POS filter.
 *
 * Example configuration:
 * ```json
 * {
 *   "filter": {
 *     "pos_filter": {
 *       "type": "kiwi_part_of_speech",
 *       "stop_tags": ["JKS", "JKO", "JX", "EP", "EF", "EC"]
 *     }
 *   }
 * }
 * ```
 */
@AnalysisSettings
interface KiwiPartOfSpeechSettings {

    /**
     * POS tags to filter out (stop tags).
     * Default: empty (uses POSTagSet.DEFAULT_STOP_TAGS if empty)
     */
    @ListSetting(path = "stop_tags")
    fun stopTags(): List<String>
}

/**
 * Configuration settings for the Kiwi analyzer.
 *
 * The analyzer combines a Kiwi tokenizer with optional filters.
 *
 * Example configuration:
 * ```json
 * {
 *   "analyzer": {
 *     "korean_analyzer": {
 *       "type": "kiwi",
 *       "model_path": "/etc/elasticsearch/kiwi",
 *       "stop_tags": ["JKS", "JKO", "JX"]
 *     }
 *   }
 * }
 * ```
 */
@AnalysisSettings
interface KiwiAnalyzerSettings {

    @StringSetting(path = "model_path", defaultValue = "kiwi")
    fun modelPath(): String

    @IntSetting(path = "num_threads", defaultValue = 0)
    fun numThreads(): Int

    @BooleanSetting(path = "discard_punctuation", defaultValue = true)
    fun discardPunctuation(): Boolean

    @StringSetting(path = "user_dictionary", defaultValue = "")
    fun userDictionary(): String

    @ListSetting(path = "stop_tags")
    fun stopTags(): List<String>
}
