package com.brandazine.elasticsearch.analysis.kiwi

/**
 * Elasticsearch plugin that provides Korean morphological analysis using Kiwi.
 *
 * This plugin registers the following analysis components via @NamedComponent annotations:
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
 * @see KiwiAnalyzerFactory
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
 *
 * Note: In the ES stable plugin API, components are auto-discovered via
 * @NamedComponent annotations. This class serves as documentation and
 * can be extended if additional plugin lifecycle hooks are needed.
 */
class KiwiAnalysisPlugin
