package com.brandazine.elasticsearch.analysis.kiwi

import org.elasticsearch.SpecialPermission
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Logger

/**
 * Handles loading of native libraries required by KiwiJava.
 *
 * KiwiJava bundles platform-specific native libraries inside the JAR file.
 * This loader ensures the native library is loaded exactly once before
 * any Kiwi operations are performed.
 *
 * Note: Uses Elasticsearch's SpecialPermission and AccessController.doPrivileged
 * to allow native library loading under Elasticsearch's SecurityManager.
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
     * Uses Elasticsearch's SpecialPermission pattern followed by
     * AccessController.doPrivileged to grant permissions defined
     * in the plugin's security.policy file for native library loading.
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
            // Check that caller has permission to perform privileged operations
            // This is required by Elasticsearch's security model
            @Suppress("DEPRECATION")
            val sm = System.getSecurityManager()
            if (sm != null) {
                sm.checkPermission(SpecialPermission())
            }

            // Use AccessController.doPrivileged to allow native library loading
            // under Elasticsearch's SecurityManager with our security.policy permissions
            @Suppress("DEPRECATION")
            AccessController.doPrivileged(PrivilegedAction {
                // KiwiJava uses JNI and loads its native library automatically
                // when the Kiwi class is first accessed. We trigger this by
                // loading the class explicitly.
                Class.forName("kr.pe.bab2min.Kiwi")
            })

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
        } catch (e: Exception) {
            // Catch any other exceptions (including those wrapped by doPrivileged)
            loadError = e
            val cause = if (e.cause != null) e.cause else e
            logger.severe("Failed to load Kiwi: ${cause?.message}")
            throw RuntimeException("Failed to load Kiwi native library", cause)
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
        @Suppress("DEPRECATION")
        val sm = System.getSecurityManager()
        if (sm != null) {
            sm.checkPermission(SpecialPermission())
        }
        @Suppress("DEPRECATION")
        return AccessController.doPrivileged(PrivilegedAction {
            val os = System.getProperty("os.name")
            val arch = System.getProperty("os.arch")
            val javaVersion = System.getProperty("java.version")
            "OS: $os, Arch: $arch, Java: $javaVersion"
        })
    }
}
