package com.brandazine.elasticsearch.analysis.kiwi

import org.apache.lucene.analysis.FilteringTokenFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.TypeAttribute

/**
 * Token filter that removes tokens based on their POS (Part-of-Speech) tag.
 *
 * This filter uses the token type attribute (set by KiwiTokenizer to the POS tag)
 * to determine which tokens to keep or remove.
 *
 * Typical usage is to remove grammatical particles, endings, and punctuation
 * that don't contribute to semantic meaning in search.
 *
 * Example: Filter out particles and endings for content-focused search
 * ```
 * stop_tags: ["JKS", "JKO", "JX", "EP", "EF", "EC", "SF", "SP"]
 * ```
 *
 * Input tokens:  "나는" (NP) -> "는" (JX) -> "학교" (NNG) -> "에" (JKB) -> "간다" (VV+EC)
 * Output tokens: "나"        ->            -> "학교"       ->           -> "가"
 */
class KiwiPartOfSpeechFilter(
    input: TokenStream,
    private val stopTags: Set<String>
) : FilteringTokenFilter(input) {

    private val typeAtt: TypeAttribute = addAttribute(TypeAttribute::class.java)

    /**
     * Determine whether to accept or reject the current token.
     *
     * @return true if the token should be kept, false if it should be filtered out
     */
    override fun accept(): Boolean {
        val posTag = typeAtt.type()

        // Accept token if its POS tag is not in the stop list
        return posTag !in stopTags
    }

    companion object {
        /**
         * Create a filter with default stop tags (particles, endings, punctuation).
         */
        fun withDefaultStopTags(input: TokenStream): KiwiPartOfSpeechFilter {
            return KiwiPartOfSpeechFilter(input, POSTagSet.DEFAULT_STOP_TAGS)
        }

        /**
         * Create a filter that keeps only content words (nouns, verbs, adjectives).
         */
        fun contentWordsOnly(input: TokenStream): KiwiPartOfSpeechFilter {
            // Stop tags = everything except content words
            val stopTags = (POSTagSet.PARTICLES +
                          POSTagSet.ENDINGS +
                          POSTagSet.PUNCTUATION +
                          POSTagSet.AFFIXES).toSet()
            return KiwiPartOfSpeechFilter(input, stopTags)
        }
    }
}
