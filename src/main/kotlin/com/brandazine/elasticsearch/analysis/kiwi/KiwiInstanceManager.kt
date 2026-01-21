package com.brandazine.elasticsearch.analysis.kiwi

import kr.pe.bab2min.Kiwi
import kr.pe.bab2min.KiwiBuilder
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * Manages Kiwi morphological analyzer instances.
 *
 * Kiwi instances are expensive to create (loading model files takes time and memory).
 * This manager caches instances by their configuration key, allowing multiple tokenizers
 * with the same settings to share a single Kiwi instance.
 *
 * Instances are thread-safe - Kiwi supports concurrent tokenization.
 */
object KiwiInstanceManager {

    private val logger = Logger.getLogger(KiwiInstanceManager::class.java.name)
    private val instances = ConcurrentHashMap<String, Kiwi>()

    /**
     * Get or create a Kiwi instance with the specified configuration.
     *
     * @param modelPath Path to the Kiwi model directory
     * @param numThreads Number of worker threads (0 = auto-detect)
     * @param userDictionary Optional path to user dictionary file
     * @return A configured Kiwi instance
     */
    fun getInstance(
        modelPath: String,
        numThreads: Int = 0,
        userDictionary: String? = null
    ): Kiwi {
        val key = buildCacheKey(modelPath, numThreads, userDictionary)

        return instances.computeIfAbsent(key) {
            createInstance(modelPath, numThreads, userDictionary)
        }
    }

    /**
     * Build a cache key from configuration parameters.
     */
    private fun buildCacheKey(
        modelPath: String,
        numThreads: Int,
        userDictionary: String?
    ): String {
        return "$modelPath|$numThreads|${userDictionary ?: ""}"
    }

    /**
     * Create a new Kiwi instance with the given configuration.
     */
    private fun createInstance(
        modelPath: String,
        numThreads: Int,
        userDictionary: String?
    ): Kiwi {
        // Ensure native library is loaded
        NativeLibraryLoader.load()

        logger.info("Creating Kiwi instance: modelPath=$modelPath, threads=$numThreads, userDict=$userDictionary")

        val builder = KiwiBuilder(modelPath, numThreads)

        // Load user dictionary if specified
        userDictionary?.let { dictPath ->
            if (dictPath.isNotEmpty()) {
                loadUserDictionary(builder, dictPath)
            }
        }

        val kiwi = builder.build()

        logger.info("Kiwi instance created successfully")
        return kiwi
    }

    /**
     * Load a user dictionary file into the Kiwi builder.
     *
     * Dictionary file format (tab-separated):
     * ```
     * word<TAB>POS_tag<TAB>score
     * ```
     *
     * Example:
     * ```
     * 브랜다진	NNP	0.0
     * 딥러닝	NNG	0.0
     * ChatGPT	SL	0.0
     * ```
     */
    private fun loadUserDictionary(builder: KiwiBuilder, dictPath: String) {
        val dictFile = File(dictPath)

        if (!dictFile.exists()) {
            throw IllegalArgumentException("User dictionary file not found: $dictPath")
        }

        logger.info("Loading user dictionary: $dictPath")

        var wordCount = 0
        dictFile.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()

                // Skip empty lines and comments
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    return@forEach
                }

                val parts = trimmed.split("\t")
                if (parts.size < 2) {
                    logger.warning("Invalid dictionary line (expected at least 2 tab-separated fields): $line")
                    return@forEach
                }

                val word = parts[0]
                val posTagName = parts[1].uppercase()
                val score = if (parts.size >= 3) parts[2].toFloatOrNull() ?: 0.0f else 0.0f

                try {
                    // Convert POS tag name to byte using POSTag constants
                    val posTagByte = posTagNameToByte(posTagName)
                    builder.addWord(word, posTagByte, score)
                    wordCount++
                } catch (e: Exception) {
                    logger.warning("Failed to add word '$word' with tag '$posTagName': ${e.message}")
                }
            }
        }

        logger.info("Loaded $wordCount words from user dictionary")
    }

    /**
     * Convert a POS tag name (e.g., "NNG", "VV") to the byte constant used by Kiwi.
     */
    private fun posTagNameToByte(tagName: String): Byte {
        return when (tagName) {
            "NNG" -> Kiwi.POSTag.nng
            "NNP" -> Kiwi.POSTag.nnp
            "NNB" -> Kiwi.POSTag.nnb
            "NR" -> Kiwi.POSTag.nr
            "NP" -> Kiwi.POSTag.np
            "VV" -> Kiwi.POSTag.vv
            "VA" -> Kiwi.POSTag.va
            "VX" -> Kiwi.POSTag.vx
            "VCP" -> Kiwi.POSTag.vcp
            "VCN" -> Kiwi.POSTag.vcn
            "MM" -> Kiwi.POSTag.mm
            "MAG" -> Kiwi.POSTag.mag
            "MAJ" -> Kiwi.POSTag.maj
            "IC" -> Kiwi.POSTag.ic
            "JKS" -> Kiwi.POSTag.jks
            "JKC" -> Kiwi.POSTag.jkc
            "JKG" -> Kiwi.POSTag.jkg
            "JKO" -> Kiwi.POSTag.jko
            "JKB" -> Kiwi.POSTag.jkb
            "JKV" -> Kiwi.POSTag.jkv
            "JKQ" -> Kiwi.POSTag.jkq
            "JX" -> Kiwi.POSTag.jx
            "JC" -> Kiwi.POSTag.jc
            "EP" -> Kiwi.POSTag.ep
            "EF" -> Kiwi.POSTag.ef
            "EC" -> Kiwi.POSTag.ec
            "ETN" -> Kiwi.POSTag.etn
            "ETM" -> Kiwi.POSTag.etm
            "XPN" -> Kiwi.POSTag.xpn
            "XSN" -> Kiwi.POSTag.xsn
            "XSV" -> Kiwi.POSTag.xsv
            "XSA" -> Kiwi.POSTag.xsa
            "XR" -> Kiwi.POSTag.xr
            "SF" -> Kiwi.POSTag.sf
            "SP" -> Kiwi.POSTag.sp
            "SS" -> Kiwi.POSTag.ss
            "SE" -> Kiwi.POSTag.se
            "SO" -> Kiwi.POSTag.so
            "SW" -> Kiwi.POSTag.sw
            "SL" -> Kiwi.POSTag.sl
            "SH" -> Kiwi.POSTag.sh
            "SN" -> Kiwi.POSTag.sn
            else -> Kiwi.POSTag.unknown
        }
    }

    /**
     * Get the number of cached instances.
     * Useful for monitoring and testing.
     */
    fun getCachedInstanceCount(): Int = instances.size

    /**
     * Clear all cached instances.
     * Use with caution - this will invalidate references held by tokenizers.
     */
    fun clearCache() {
        logger.info("Clearing Kiwi instance cache (${instances.size} instances)")
        instances.clear()
    }

    /**
     * Check if an instance with the given configuration is cached.
     */
    fun isCached(
        modelPath: String,
        numThreads: Int = 0,
        userDictionary: String? = null
    ): Boolean {
        val key = buildCacheKey(modelPath, numThreads, userDictionary)
        return instances.containsKey(key)
    }
}
