package com.brandazine.elasticsearch.analysis.kiwi

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.analysis.tokenattributes.TypeAttribute
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import java.io.StringReader
import kotlin.test.assertTrue

/**
 * Integration tests for KiwiTokenizer.
 *
 * These tests require:
 * 1. KiwiJava JAR in classpath
 * 2. Kiwi model files available
 *
 * Tests are skipped if Kiwi is not available.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("isKiwiAvailable")
class KiwiTokenizerIntegrationTest {

    companion object {
        private var kiwi: kr.pe.bab2min.Kiwi? = null
        private var modelPath: String? = null

        @JvmStatic
        fun isKiwiAvailable(): Boolean {
            // Check for Kiwi availability during condition evaluation
            modelPath = System.getenv("KIWI_MODEL_PATH")
                ?: System.getProperty("kiwi.model.path")
                ?: "kiwi"

            return try {
                NativeLibraryLoader.load()
                kiwi = KiwiInstanceManager.getInstance(modelPath!!)
                println("Kiwi loaded successfully from: $modelPath")
                true
            } catch (e: Exception) {
                println("Kiwi not available: ${e.message}")
                false
            }
        }
    }

    @BeforeAll
    fun setup() {
        // Kiwi is already loaded in isKiwiAvailable()
        // This ensures it's available for all tests
        if (kiwi == null) {
            modelPath = System.getenv("KIWI_MODEL_PATH")
                ?: System.getProperty("kiwi.model.path")
                ?: "kiwi"
            NativeLibraryLoader.load()
            kiwi = KiwiInstanceManager.getInstance(modelPath!!)
        }
    }

    @Test
    fun `tokenizer produces tokens for Korean text`() {
        val tokenizer = KiwiTokenizer(kiwi!!, discardPunctuation = true)

        tokenizer.setReader(StringReader("안녕하세요"))
        tokenizer.reset()

        val tokens = collectTokens(tokenizer)

        assertTrue(tokens.isNotEmpty(), "Should produce at least one token")
        tokenizer.close()
    }

    @Test
    fun `tokenizer sets correct offsets`() {
        val tokenizer = KiwiTokenizer(kiwi!!, discardPunctuation = true)

        tokenizer.setReader(StringReader("한국어"))
        tokenizer.reset()

        val offsetAtt = tokenizer.getAttribute(OffsetAttribute::class.java)

        if (tokenizer.incrementToken()) {
            val startOffset = offsetAtt.startOffset()
            val endOffset = offsetAtt.endOffset()

            assertTrue(startOffset >= 0, "Start offset should be non-negative")
            assertTrue(endOffset > startOffset, "End offset should be greater than start")
            assertTrue(endOffset <= "한국어".length, "End offset should not exceed input length")
        }

        tokenizer.close()
    }

    @Test
    fun `tokenizer sets POS tag as type`() {
        val tokenizer = KiwiTokenizer(kiwi!!, discardPunctuation = true)

        tokenizer.setReader(StringReader("학교"))
        tokenizer.reset()

        val typeAtt = tokenizer.getAttribute(TypeAttribute::class.java)

        if (tokenizer.incrementToken()) {
            val posTag = typeAtt.type()
            assertTrue(posTag.isNotEmpty(), "POS tag should not be empty")
            // "학교" should be NNG (general noun)
            println("Token type: $posTag")
        }

        tokenizer.close()
    }

    @Test
    fun `tokenizer with discardPunctuation=true filters punctuation`() {
        val tokenizer = KiwiTokenizer(kiwi!!, discardPunctuation = true)

        tokenizer.setReader(StringReader("안녕하세요!"))
        tokenizer.reset()

        val tokens = collectTokens(tokenizer)

        // Should not contain "!" as a separate token
        assertTrue(tokens.none { it.term == "!" }, "Punctuation should be discarded")

        tokenizer.close()
    }

    @Test
    fun `tokenizer with discardPunctuation=false keeps punctuation`() {
        val tokenizer = KiwiTokenizer(kiwi!!, discardPunctuation = false)

        tokenizer.setReader(StringReader("안녕!"))
        tokenizer.reset()

        val tokens = collectTokens(tokenizer)
        val types = tokens.map { it.type }

        // Should include punctuation token (SF tag)
        assertTrue(types.any { it == "SF" || it == "SW" }, "Should include punctuation token")

        tokenizer.close()
    }

    @Test
    fun `tokenizer with posTagsToInclude filters by POS`() {
        // Only include nouns
        val tokenizer = KiwiTokenizer(
            kiwi!!,
            discardPunctuation = true,
            posTagsToInclude = setOf("NNG", "NNP")
        )

        tokenizer.setReader(StringReader("나는 학교에 갔다"))
        tokenizer.reset()

        val tokens = collectTokens(tokenizer)

        // All tokens should be NNG or NNP
        assertTrue(tokens.all { it.type in setOf("NNG", "NNP") },
            "All tokens should be nouns, got: ${tokens.map { "${it.term}/${it.type}" }}")

        tokenizer.close()
    }

    @Test
    fun `tokenizer handles empty input`() {
        val tokenizer = KiwiTokenizer(kiwi!!)

        tokenizer.setReader(StringReader(""))
        tokenizer.reset()

        val tokens = collectTokens(tokenizer)

        assertTrue(tokens.isEmpty(), "Empty input should produce no tokens")

        tokenizer.close()
    }

    @Test
    fun `tokenizer handles mixed Korean and English`() {
        val tokenizer = KiwiTokenizer(kiwi!!, discardPunctuation = true)

        tokenizer.setReader(StringReader("Hello 세계"))
        tokenizer.reset()

        val tokens = collectTokens(tokenizer)

        assertTrue(tokens.isNotEmpty(), "Should produce tokens for mixed text")
        // Should have at least "Hello" (SL) and "세계" (NNG)
        assertTrue(tokens.any { it.term.contains("Hello") || it.term == "Hello" },
            "Should include English word")

        tokenizer.close()
    }

    private data class TokenInfo(
        val term: String,
        val type: String,
        val startOffset: Int,
        val endOffset: Int
    )

    private fun collectTokens(tokenizer: KiwiTokenizer): List<TokenInfo> {
        val termAtt = tokenizer.getAttribute(CharTermAttribute::class.java)
        val typeAtt = tokenizer.getAttribute(TypeAttribute::class.java)
        val offsetAtt = tokenizer.getAttribute(OffsetAttribute::class.java)

        val tokens = mutableListOf<TokenInfo>()
        while (tokenizer.incrementToken()) {
            tokens.add(
                TokenInfo(
                    term = termAtt.toString(),
                    type = typeAtt.type(),
                    startOffset = offsetAtt.startOffset(),
                    endOffset = offsetAtt.endOffset()
                )
            )
        }
        return tokens
    }
}
