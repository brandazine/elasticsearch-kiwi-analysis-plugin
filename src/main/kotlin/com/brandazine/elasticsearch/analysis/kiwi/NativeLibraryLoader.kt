package com.brandazine.elasticsearch.analysis.kiwi

import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Logger

/**
 * Handles loading of native libraries required by KiwiJava.
 *
 * KiwiJava bundles platform-specific native libraries inside the JAR file.
 * This loader ensures the native library is loaded exactly once before
 * any Kiwi operations are performed.
 */
object NativeLibraryLoader {

    private val logger = Logger.getLogger(NativeLibraryLoader::class.java.name)
    private val loaded = AtomicBoolean(false)
    private var loadError: Throwable? = null

    /**
     * Ensures the native library is loaded.
     *
     * This method is thread-safe and idempotent - it will only attempt
     * to load the library once, even if called from multiple threads.
     *
     * @throws RuntimeException if the native library fails to load
     */
    @Synchronized
    fun load() {
        if (loaded.get()) {
            loadError?.let { throw RuntimeException("Native library failed to load previously", it) }
            return
        }

        try {
            // KiwiJava uses JNI and loads its native library automatically
            // when the Kiwi class is first accessed. We trigger this by
            // loading the class explicitly.
            Class.forName("kr.pe.bab2min.Kiwi")

            logger.info("Kiwi native library loaded successfully")
            loaded.set(true)
        } catch (e: UnsatisfiedLinkError) {
            loadError = e
            logger.severe("Failed to load Kiwi native library: ${e.message}")
            throw RuntimeException(
                "Failed to load Kiwi native library. " +
                "Ensure the correct platform-specific kiwi-java JAR is in the classpath. " +
                "Platform: ${System.getProperty("os.name")} / ${System.getProperty("os.arch")}",
                e
            )
        } catch (e: ClassNotFoundException) {
            loadError = e
            logger.severe("KiwiJava classes not found: ${e.message}")
            throw RuntimeException(
                "KiwiJava classes not found. Ensure kiwi-java JAR is in the classpath.",
                e
            )
        }
    }

    /**
     * Check if the native library has been successfully loaded.
     */
    fun isLoaded(): Boolean = loaded.get() && loadError == null

    /**
     * Get platform information for debugging.
     */
    fun getPlatformInfo(): String {
        val os = System.getProperty("os.name")
        val arch = System.getProperty("os.arch")
        val javaVersion = System.getProperty("java.version")
        return "OS: $os, Arch: $arch, Java: $javaVersion"
    }
}
