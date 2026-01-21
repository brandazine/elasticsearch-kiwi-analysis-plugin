package com.brandazine.elasticsearch.analysis.kiwi

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests to verify ES Stable Plugin API compliance using reflection.
 * (ES API classes are compileOnly, so we use string-based annotation checks)
 */
class StablePluginApiTest {

    @Test
    fun `KiwiTokenizerFactory has correct NamedComponent annotation`() {
        val annotation = findAnnotation(KiwiTokenizerFactory::class.java, "NamedComponent")
        assertNotNull(annotation, "KiwiTokenizerFactory must have @NamedComponent annotation")

        val value = annotation.annotationClass.java.getMethod("value").invoke(annotation) as String
        assertEquals("kiwi", value, "Component name must be 'kiwi'")
    }

    @Test
    fun `KiwiTokenizerFactory implements TokenizerFactory`() {
        val interfaces = KiwiTokenizerFactory::class.java.interfaces.map { it.simpleName }
        assertTrue(interfaces.contains("TokenizerFactory"), "Must implement TokenizerFactory")
    }

    @Test
    fun `KiwiPartOfSpeechFilterFactory has correct NamedComponent annotation`() {
        val annotation = findAnnotation(KiwiPartOfSpeechFilterFactory::class.java, "NamedComponent")
        assertNotNull(annotation, "KiwiPartOfSpeechFilterFactory must have @NamedComponent annotation")

        val value = annotation.annotationClass.java.getMethod("value").invoke(annotation) as String
        assertEquals("kiwi_part_of_speech", value, "Component name must be 'kiwi_part_of_speech'")
    }

    @Test
    fun `KiwiPartOfSpeechFilterFactory implements TokenFilterFactory`() {
        val interfaces = KiwiPartOfSpeechFilterFactory::class.java.interfaces.map { it.simpleName }
        assertTrue(interfaces.contains("TokenFilterFactory"), "Must implement TokenFilterFactory")
    }

    @Test
    fun `KiwiAnalyzerFactory has correct NamedComponent annotation`() {
        val annotation = findAnnotation(KiwiAnalyzerFactory::class.java, "NamedComponent")
        assertNotNull(annotation, "KiwiAnalyzerFactory must have @NamedComponent annotation")

        val value = annotation.annotationClass.java.getMethod("value").invoke(annotation) as String
        assertEquals("kiwi", value, "Component name must be 'kiwi'")
    }

    @Test
    fun `KiwiAnalyzerFactory implements AnalyzerFactory`() {
        val interfaces = KiwiAnalyzerFactory::class.java.interfaces.map { it.simpleName }
        assertTrue(interfaces.contains("AnalyzerFactory"), "Must implement AnalyzerFactory")
    }

    @Test
    fun `Settings interfaces have AnalysisSettings annotation`() {
        assertNotNull(
            findAnnotation(KiwiTokenizerSettings::class.java, "AnalysisSettings"),
            "KiwiTokenizerSettings must have @AnalysisSettings annotation"
        )
        assertNotNull(
            findAnnotation(KiwiPartOfSpeechSettings::class.java, "AnalysisSettings"),
            "KiwiPartOfSpeechSettings must have @AnalysisSettings annotation"
        )
        assertNotNull(
            findAnnotation(KiwiAnalyzerSettings::class.java, "AnalysisSettings"),
            "KiwiAnalyzerSettings must have @AnalysisSettings annotation"
        )
    }

    private fun findAnnotation(clazz: Class<*>, annotationSimpleName: String): Annotation? {
        return clazz.annotations.find { it.annotationClass.simpleName == annotationSimpleName }
    }
}
