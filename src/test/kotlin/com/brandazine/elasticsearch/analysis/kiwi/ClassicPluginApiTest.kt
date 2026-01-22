package com.brandazine.elasticsearch.analysis.kiwi

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests to verify ES Classic Plugin API compliance.
 * Verifies that the plugin class and factories follow the classic plugin pattern.
 */
class ClassicPluginApiTest {

    @Test
    fun `KiwiAnalysisPlugin extends Plugin`() {
        val superclass = KiwiAnalysisPlugin::class.java.superclass
        assertNotNull(superclass)
        assertEquals("Plugin", superclass.simpleName,
            "KiwiAnalysisPlugin must extend Plugin")
    }

    @Test
    fun `KiwiAnalysisPlugin implements AnalysisPlugin`() {
        val interfaces = KiwiAnalysisPlugin::class.java.interfaces.map { it.simpleName }
        assertTrue(interfaces.contains("AnalysisPlugin"),
            "KiwiAnalysisPlugin must implement AnalysisPlugin")
    }

    @Test
    fun `KiwiAnalysisPlugin getTokenizers returns kiwi tokenizer`() {
        val plugin = KiwiAnalysisPlugin()
        val tokenizers = plugin.getTokenizers()

        assertTrue(tokenizers.containsKey("kiwi"),
            "getTokenizers must include 'kiwi' key")
    }

    @Test
    fun `KiwiAnalysisPlugin getTokenFilters returns kiwi_part_of_speech filter`() {
        val plugin = KiwiAnalysisPlugin()
        val filters = plugin.getTokenFilters()

        assertTrue(filters.containsKey("kiwi_part_of_speech"),
            "getTokenFilters must include 'kiwi_part_of_speech' key")
    }

    @Test
    fun `KiwiAnalysisPlugin getAnalyzers returns kiwi analyzer`() {
        val plugin = KiwiAnalysisPlugin()
        val analyzers = plugin.getAnalyzers()

        assertTrue(analyzers.containsKey("kiwi"),
            "getAnalyzers must include 'kiwi' key")
    }

    @Test
    fun `KiwiTokenizerFactory implements TokenizerFactory`() {
        val interfaces = KiwiTokenizerFactory::class.java.interfaces.map { it.simpleName }
        assertTrue(interfaces.contains("TokenizerFactory"),
            "KiwiTokenizerFactory must implement TokenizerFactory")
    }

    @Test
    fun `KiwiPartOfSpeechFilterFactory implements TokenFilterFactory`() {
        val interfaces = KiwiPartOfSpeechFilterFactory::class.java.interfaces.map { it.simpleName }
        assertTrue(interfaces.contains("TokenFilterFactory"),
            "KiwiPartOfSpeechFilterFactory must implement TokenFilterFactory")
    }

    @Test
    fun `KiwiAnalyzerProvider implements AnalyzerProvider`() {
        val interfaces = KiwiAnalyzerProvider::class.java.interfaces.map { it.simpleName }
        assertTrue(interfaces.contains("AnalyzerProvider"),
            "KiwiAnalyzerProvider must implement AnalyzerProvider")
    }
}
