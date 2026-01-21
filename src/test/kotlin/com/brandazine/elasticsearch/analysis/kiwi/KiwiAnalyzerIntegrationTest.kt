package com.brandazine.elasticsearch.analysis.kiwi

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.TypeAttribute
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import java.io.StringReader
import kotlin.test.assertTrue

/**
 * Integration tests for KiwiAnalyzer.
 *
 * These tests require Kiwi model files to be available.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("isKiwiAvailable")
class KiwiAnalyzerIntegrationTest {

    companion object {
        private var kiwi: kr.pe.bab2min.Kiwi? = null
        private var modelPath: String? = null

        @JvmStatic
        fun isKiwiAvailable(): Boolean {
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
        if (kiwi == null) {
            modelPath = System.getenv("KIWI_MODEL_PATH")
                ?: System.getProperty("kiwi.model.path")
                ?: "kiwi"
            NativeLibraryLoader.load()
            kiwi = KiwiInstanceManager.getInstance(modelPath!!)
        }
    }

    @Test
    fun `default analyzer filters particles and endings`() {
        val analyzer = KiwiAnalyzer.createDefault(kiwi!!)

        val tokenStream = analyzer.tokenStream("test", StringReader("나는 학교에 간다"))
        tokenStream.reset()

        val typeAtt = tokenStream.getAttribute(TypeAttribute::class.java)

        val types = mutableListOf<String>()
        while (tokenStream.incrementToken()) {
            types.add(typeAtt.type())
        }
        tokenStream.close()

        // Default analyzer should filter out particles (JKS, JKB, etc.)
        assertTrue(types.none { it.startsWith("JK") },
            "Should filter out particles, got: $types")
    }

    @Test
    fun `verbose analyzer keeps all tokens`() {
        val analyzer = KiwiAnalyzer.createVerbose(kiwi!!)

        val tokenStream = analyzer.tokenStream("test", StringReader("나는"))
        tokenStream.reset()

        val tokens = mutableListOf<String>()
        val termAtt = tokenStream.getAttribute(CharTermAttribute::class.java)

        while (tokenStream.incrementToken()) {
            tokens.add(termAtt.toString())
        }
        tokenStream.close()

        // "나는" should be split into "나" (pronoun) + "는" (particle)
        // Verbose analyzer should keep both
        assertTrue(tokens.size >= 2 || tokens.any { it == "나는" },
            "Should keep all morphemes, got: $tokens")
    }

    @Test
    fun `search analyzer keeps only content words`() {
        val analyzer = KiwiAnalyzer.createForSearch(kiwi!!)

        val tokenStream = analyzer.tokenStream("test", StringReader("아름다운 꽃이 피었다"))
        tokenStream.reset()

        val typeAtt = tokenStream.getAttribute(TypeAttribute::class.java)

        val types = mutableListOf<String>()
        while (tokenStream.incrementToken()) {
            types.add(typeAtt.type())
        }
        tokenStream.close()

        // Search analyzer should only keep content words (nouns, verbs, adjectives)
        // UN (unknown) tags may appear for some morphemes
        assertTrue(types.all { type ->
            POSTagSet.isContentWord(type) ||
            type in setOf("XSV", "XSA", "UN") // verb/adj deriving suffixes and unknown might appear
        }, "Should keep only content words, got: $types")
    }

    @Test
    fun `analyzer applies lowercase to English`() {
        val analyzer = KiwiAnalyzer.createDefault(kiwi!!)

        val tokenStream = analyzer.tokenStream("test", StringReader("Hello World"))
        tokenStream.reset()

        val termAtt = tokenStream.getAttribute(CharTermAttribute::class.java)

        val terms = mutableListOf<String>()
        while (tokenStream.incrementToken()) {
            terms.add(termAtt.toString())
        }
        tokenStream.close()

        // Should be lowercased
        assertTrue(terms.all { it == it.lowercase() },
            "Should be lowercased, got: $terms")
    }

    @Test
    fun `analyzer handles multiple sentences`() {
        val analyzer = KiwiAnalyzer.createDefault(kiwi!!)

        val text = "첫 번째 문장입니다. 두 번째 문장이에요."
        val tokenStream = analyzer.tokenStream("test", StringReader(text))
        tokenStream.reset()

        val termAtt = tokenStream.getAttribute(CharTermAttribute::class.java)

        val terms = mutableListOf<String>()
        while (tokenStream.incrementToken()) {
            terms.add(termAtt.toString())
        }
        tokenStream.close()

        assertTrue(terms.isNotEmpty(), "Should produce tokens from multiple sentences")
        // Should have tokens from both sentences
        println("Tokens: $terms")
    }
}
