package com.brandazine.elasticsearch.analysis.kiwi

import org.apache.lucene.analysis.Analyzer
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.AnalyzerProvider
import org.elasticsearch.index.analysis.TokenFilterFactory
import org.elasticsearch.index.analysis.TokenizerFactory
import org.elasticsearch.indices.analysis.AnalysisModule
import org.elasticsearch.plugins.AnalysisPlugin
import org.elasticsearch.plugins.Plugin

/**
 * Elasticsearch plugin that provides Korean morphological analysis using Kiwi.
 *
 * This classic plugin registers the following analysis components:
 *
 * ## Tokenizer: "kiwi"
 * Morphological tokenizer using Kiwi analyzer.
 * @see KiwiTokenizerFactory
 *
 * ## Token Filter: "kiwi_part_of_speech"
 * Filters tokens by POS tag.
 * @see KiwiPartOfSpeechFilterFactory
 *
 * ## Analyzer: "kiwi"
 * Complete analyzer combining tokenizer + POS filter + lowercase.
 * @see KiwiAnalyzerProvider
 *
 * ## Installation
 * 1. Download the appropriate platform-specific plugin ZIP
 * 2. Install: bin/elasticsearch-plugin install file:///path/to/analysis-kiwi.zip
 * 3. Place Kiwi model files in {ES_CONFIG}/kiwi/
 * 4. Restart Elasticsearch
 *
 * ## Example Configuration
 * ```json
 * {
 *   "settings": {
 *     "analysis": {
 *       "analyzer": {
 *         "korean": {
 *           "type": "kiwi",
 *           "model_path": "/etc/elasticsearch/kiwi"
 *         }
 *       }
 *     }
 *   }
 * }
 * ```
 */
class KiwiAnalysisPlugin : Plugin(), AnalysisPlugin {

    override fun getTokenizers(): Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> {
        return mapOf(
            "kiwi" to AnalysisModule.AnalysisProvider { indexSettings, env, name, settings ->
                KiwiTokenizerFactory(indexSettings, env, name, settings)
            }
        )
    }

    override fun getTokenFilters(): Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> {
        return mapOf(
            "kiwi_part_of_speech" to AnalysisModule.AnalysisProvider { indexSettings, env, name, settings ->
                KiwiPartOfSpeechFilterFactory(indexSettings, env, name, settings)
            }
        )
    }

    override fun getAnalyzers(): Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<out Analyzer>>> {
        return mapOf(
            "kiwi" to AnalysisModule.AnalysisProvider { indexSettings, env, name, settings ->
                KiwiAnalyzerProvider(indexSettings, env, name, settings)
            }
        )
    }
}
