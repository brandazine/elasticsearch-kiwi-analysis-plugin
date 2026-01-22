package com.brandazine.elasticsearch.analysis.kiwi

import kr.pe.bab2min.Kiwi
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.Tokenizer

/**
 * Lucene Analyzer for Korean text using Kiwi morphological analyzer.
 *
 * This analyzer provides a complete analysis pipeline:
 * 1. Tokenization using Kiwi morphological analysis
 * 2. POS-based filtering (optional)
 * 3. Lowercase normalization
 *
 * For more control, use KiwiTokenizer directly with custom filter chains.
 *
 * Example usage:
 * ```kotlin
 * val analyzer = KiwiAnalyzer(kiwi)
 * val tokenStream = analyzer.tokenStream("field", "안녕하세요 한국어 분석입니다")
 * ```
 */
class KiwiAnalyzer(
    private val kiwi: Kiwi,
    private val discardPunctuation: Boolean = true,
    private val posTagsToInclude: Set<String>? = null,
    private val stopTags: Set<String> = POSTagSet.DEFAULT_STOP_TAGS,
    private val applyPosFilter: Boolean = true,
    private val applyLowercase: Boolean = true
) : Analyzer() {

    /**
     * Create the token stream components for analysis.
     *
     * @param fieldName The field being analyzed (not used, but required by Lucene API)
     * @return TokenStreamComponents containing the tokenizer and filter chain
     */
    override fun createComponents(fieldName: String?): TokenStreamComponents {
        // Create the tokenizer
        val tokenizer: Tokenizer = KiwiTokenizer(
            kiwi = kiwi,
            discardPunctuation = discardPunctuation,
            posTagsToInclude = posTagsToInclude
        )

        // Build the filter chain
        var tokenStream: TokenStream = tokenizer

        // Apply POS filter if enabled
        if (applyPosFilter && stopTags.isNotEmpty()) {
            tokenStream = KiwiPartOfSpeechFilter(tokenStream, stopTags)
        }

        // Apply lowercase filter if enabled
        if (applyLowercase) {
            tokenStream = LowerCaseFilter(tokenStream)
        }

        return TokenStreamComponents(tokenizer, tokenStream)
    }

    companion object {
        /**
         * Create an analyzer with default settings.
         * - Discards punctuation
         * - Filters out particles and endings
         * - Applies lowercase
         */
        fun createDefault(kiwi: Kiwi): KiwiAnalyzer {
            return KiwiAnalyzer(kiwi)
        }

        /**
         * Create an analyzer that keeps all tokens (no filtering).
         * Useful for debugging or when you need all morphemes.
         */
        fun createVerbose(kiwi: Kiwi): KiwiAnalyzer {
            return KiwiAnalyzer(
                kiwi = kiwi,
                discardPunctuation = false,
                applyPosFilter = false,
                applyLowercase = false
            )
        }

        /**
         * Create an analyzer optimized for search.
         * - Keeps only content words (nouns, verbs, adjectives)
         * - Discards particles, endings, punctuation
         */
        fun createForSearch(kiwi: Kiwi): KiwiAnalyzer {
            return KiwiAnalyzer(
                kiwi = kiwi,
                discardPunctuation = true,
                stopTags = POSTagSet.PARTICLES + POSTagSet.ENDINGS + POSTagSet.PUNCTUATION,
                applyPosFilter = true,
                applyLowercase = true
            )
        }
    }
}
