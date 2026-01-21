package com.brandazine.elasticsearch.analysis.kiwi

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * Simple test to verify Kiwi loading.
 */
class KiwiLoadingTest {

    @Test
    fun `check kiwi class is available`() {
        try {
            val clazz = Class.forName("kr.pe.bab2min.Kiwi")
            println("Kiwi class found: $clazz")
            assertTrue(true)
        } catch (e: ClassNotFoundException) {
            println("Kiwi class NOT found: ${e.message}")
            assertTrue(false, "Kiwi class should be in classpath")
        }
    }

    @Test
    fun `check model path and try loading`() {
        val modelPath = System.getenv("KIWI_MODEL_PATH")
            ?: System.getProperty("kiwi.model.path")
            ?: "kiwi"

        println("Model path: $modelPath")
        println("Model path exists: ${java.io.File(modelPath).exists()}")
        println("Model path is directory: ${java.io.File(modelPath).isDirectory()}")

        if (java.io.File(modelPath).isDirectory()) {
            println("Contents: ${java.io.File(modelPath).listFiles()?.map { it.name }}")
        }

        try {
            NativeLibraryLoader.load()
            println("Native library loaded successfully")

            val kiwi = KiwiInstanceManager.getInstance(modelPath)
            println("Kiwi instance created: $kiwi")

            // Try tokenizing
            val analyzeOption = kr.pe.bab2min.Kiwi.AnalyzeOption(kr.pe.bab2min.Kiwi.Match.all)
            val tokens = kiwi.tokenize("안녕하세요", analyzeOption)
            println("Tokenization result: ${tokens.map { "${it.form}/${it.tag}" }}")

            assertTrue(true)
        } catch (e: Exception) {
            println("Failed to load Kiwi: ${e.message}")
            e.printStackTrace()
            assertTrue(false, "Should be able to load Kiwi: ${e.message}")
        }
    }
}
