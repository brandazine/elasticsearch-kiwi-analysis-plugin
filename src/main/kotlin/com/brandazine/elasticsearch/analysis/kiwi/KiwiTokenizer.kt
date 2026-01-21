package com.brandazine.elasticsearch.analysis.kiwi

import kr.pe.bab2min.Kiwi
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute
import org.apache.lucene.analysis.tokenattributes.TypeAttribute
import java.io.IOException

/**
 * Lucene Tokenizer that uses Kiwi Korean morphological analyzer.
 *
 * This tokenizer reads input text, performs morphological analysis using Kiwi,
 * and produces tokens for each morpheme. Each token includes:
 * - The morpheme surface form (term)
 * - Character offsets in the original text
 * - Position increment for phrase queries
 * - POS tag as the token type
 *
 * Example:
 * Input: "안녕하세요"
 * Output tokens:
 * - "안녕" (NNG, offset 0-2)
 * - "하" (XSA, offset 2-3)
 * - "시" (EP, offset 3-4)
 * - "어요" (EF, offset 4-6)
 *
 * With default settings (discardPunctuation=true, no POS filter):
 * - Punctuation tokens are skipped
 * - All morphemes are emitted
 */
class KiwiTokenizer(
    private val kiwi: Kiwi,
    private val discardPunctuation: Boolean = true,
    private val posTagsToInclude: Set<String>? = null
) : Tokenizer() {

    // Lucene token attributes
    private val termAtt: CharTermAttribute = addAttribute(CharTermAttribute::class.java)
    private val offsetAtt: OffsetAttribute = addAttribute(OffsetAttribute::class.java)
    private val posIncrAtt: PositionIncrementAttribute = addAttribute(PositionIncrementAttribute::class.java)
    private val typeAtt: TypeAttribute = addAttribute(TypeAttribute::class.java)

    // Token buffer from Kiwi analysis
    private var tokens: List<KiwiToken> = emptyList()
    private var tokenIndex: Int = 0

    // Position tracking for gaps caused by filtered tokens
    private var pendingPosIncrement: Int = 1

    /**
     * Internal representation of a Kiwi token.
     */
    private data class KiwiToken(
        val form: String,
        val tag: String,
        val tagByte: Byte,
        val startOffset: Int,
        val endOffset: Int
    )

    /**
     * Advances to the next token in the stream.
     *
     * @return true if a token was emitted, false if end of stream
     */
    @Throws(IOException::class)
    override fun incrementToken(): Boolean {
        clearAttributes()

        while (tokenIndex < tokens.size) {
            val token = tokens[tokenIndex++]

            // Check if this token should be filtered out
            if (shouldFilter(token)) {
                // Accumulate position increment for filtered tokens
                pendingPosIncrement++
                continue
            }

            // Emit the token
            termAtt.setEmpty().append(token.form)
            offsetAtt.setOffset(
                correctOffset(token.startOffset),
                correctOffset(token.endOffset)
            )
            posIncrAtt.positionIncrement = pendingPosIncrement
            typeAtt.setType(token.tag)

            // Reset position increment for next token
            pendingPosIncrement = 1

            return true
        }

        return false
    }

    /**
     * Check if a token should be filtered out.
     */
    private fun shouldFilter(token: KiwiToken): Boolean {
        // Filter punctuation if configured
        if (discardPunctuation && isPunctuationByte(token.tagByte)) {
            return true
        }

        // Filter by POS tag whitelist if configured
        if (posTagsToInclude != null && token.tag !in posTagsToInclude) {
            return true
        }

        return false
    }

    /**
     * Check if a POS tag byte represents punctuation.
     */
    private fun isPunctuationByte(tag: Byte): Boolean {
        return tag == Kiwi.POSTag.sf ||
               tag == Kiwi.POSTag.sp ||
               tag == Kiwi.POSTag.ss ||
               tag == Kiwi.POSTag.se ||
               tag == Kiwi.POSTag.so ||
               tag == Kiwi.POSTag.sw
    }

    /**
     * Reset the tokenizer with new input.
     *
     * This is called before tokenizing a new input stream.
     * We read all input and perform Kiwi analysis here.
     */
    @Throws(IOException::class)
    override fun reset() {
        super.reset()

        // Read all input text
        val inputText = input.readText()

        // Perform morphological analysis
        tokens = if (inputText.isNotEmpty()) {
            analyzeText(inputText)
        } else {
            emptyList()
        }

        tokenIndex = 0
        pendingPosIncrement = 1
    }

    /**
     * Analyze text using Kiwi and convert to internal token format.
     */
    private fun analyzeText(text: String): List<KiwiToken> {
        val result = mutableListOf<KiwiToken>()

        try {
            // Kiwi.tokenize() returns Token[] directly
            // v0.22+ uses AnalyzeOption instead of Match int
            val analyzeOption = Kiwi.AnalyzeOption(Kiwi.Match.all)
            val kiwiTokens = kiwi.tokenize(text, analyzeOption)

            for (token in kiwiTokens) {
                val tagString = posTagByteToString(token.tag)

                result.add(
                    KiwiToken(
                        form = token.form,
                        tag = tagString,
                        tagByte = token.tag,
                        startOffset = token.position,
                        endOffset = token.position + token.length
                    )
                )
            }
        } catch (e: Exception) {
            throw IOException("Kiwi analysis failed: ${e.message}", e)
        }

        return result
    }

    /**
     * Convert a POS tag byte to its string representation.
     */
    private fun posTagByteToString(tag: Byte): String {
        return when (tag) {
            Kiwi.POSTag.nng -> "NNG"
            Kiwi.POSTag.nnp -> "NNP"
            Kiwi.POSTag.nnb -> "NNB"
            Kiwi.POSTag.nr -> "NR"
            Kiwi.POSTag.np -> "NP"
            Kiwi.POSTag.vv -> "VV"
            Kiwi.POSTag.va -> "VA"
            Kiwi.POSTag.vx -> "VX"
            Kiwi.POSTag.vcp -> "VCP"
            Kiwi.POSTag.vcn -> "VCN"
            Kiwi.POSTag.mm -> "MM"
            Kiwi.POSTag.mag -> "MAG"
            Kiwi.POSTag.maj -> "MAJ"
            Kiwi.POSTag.ic -> "IC"
            Kiwi.POSTag.jks -> "JKS"
            Kiwi.POSTag.jkc -> "JKC"
            Kiwi.POSTag.jkg -> "JKG"
            Kiwi.POSTag.jko -> "JKO"
            Kiwi.POSTag.jkb -> "JKB"
            Kiwi.POSTag.jkv -> "JKV"
            Kiwi.POSTag.jkq -> "JKQ"
            Kiwi.POSTag.jx -> "JX"
            Kiwi.POSTag.jc -> "JC"
            Kiwi.POSTag.ep -> "EP"
            Kiwi.POSTag.ef -> "EF"
            Kiwi.POSTag.ec -> "EC"
            Kiwi.POSTag.etn -> "ETN"
            Kiwi.POSTag.etm -> "ETM"
            Kiwi.POSTag.xpn -> "XPN"
            Kiwi.POSTag.xsn -> "XSN"
            Kiwi.POSTag.xsv -> "XSV"
            Kiwi.POSTag.xsa -> "XSA"
            Kiwi.POSTag.xr -> "XR"
            Kiwi.POSTag.sf -> "SF"
            Kiwi.POSTag.sp -> "SP"
            Kiwi.POSTag.ss -> "SS"
            Kiwi.POSTag.se -> "SE"
            Kiwi.POSTag.so -> "SO"
            Kiwi.POSTag.sw -> "SW"
            Kiwi.POSTag.sl -> "SL"
            Kiwi.POSTag.sh -> "SH"
            Kiwi.POSTag.sn -> "SN"
            Kiwi.POSTag.sb -> "SB"
            Kiwi.POSTag.unknown -> "UN"
            else -> "UN"
        }
    }

    /**
     * Release resources.
     */
    @Throws(IOException::class)
    override fun end() {
        super.end()

        // Set final offset to end of input
        if (tokens.isNotEmpty()) {
            val lastToken = tokens.last()
            offsetAtt.setOffset(
                correctOffset(lastToken.endOffset),
                correctOffset(lastToken.endOffset)
            )
        }
    }

    /**
     * Close the tokenizer.
     */
    @Throws(IOException::class)
    override fun close() {
        super.close()
        tokens = emptyList()
    }
}
