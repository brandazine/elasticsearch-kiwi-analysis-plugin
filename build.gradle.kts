import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    `java-library`
}

group = "com.brandazine"
version = "1.0.0-SNAPSHOT"

// ES version can be overridden via -Pes.version=9.2.2 or ES_VERSION env var
val elasticsearchVersion: String = (project.findProperty("es.version") as String?)
    ?: System.getenv("ES_VERSION")
    ?: "8.12.0"

// Lucene version mapping
val luceneVersion = when {
    elasticsearchVersion.startsWith("9.") -> "10.1.0"
    elasticsearchVersion.startsWith("8.") -> "9.9.1"
    else -> "9.9.1"
}

repositories {
    mavenCentral()
}

val kiwiJavaVersion = "0.22.2"

// Platform detection for KiwiJava JAR selection
// Can be overridden via -Pkiwi.platform=xxx or KIWI_PLATFORM env var
fun detectPlatform(): String {
    // Check Gradle property first (e.g., -Pkiwi.platform=lnx-aarch64)
    val propPlatform = project.findProperty("kiwi.platform") as String?
    if (!propPlatform.isNullOrBlank()) return propPlatform

    // Check environment variable (e.g., KIWI_PLATFORM=lnx-aarch64)
    val envPlatform = System.getenv("KIWI_PLATFORM")
    if (!envPlatform.isNullOrBlank()) return envPlatform

    // Auto-detect from system
    val os = System.getProperty("os.name").lowercase()
    val arch = System.getProperty("os.arch").lowercase()
    return when {
        os.contains("linux") && arch.contains("aarch64") -> "lnx-aarch64"
        os.contains("linux") && arch.contains("amd64") -> "lnx-x86-64"
        os.contains("mac") && arch.contains("aarch64") -> "mac-arm64"
        os.contains("mac") -> "mac-x86_64"
        os.contains("win") -> "win-x64"
        else -> throw GradleException("Unsupported platform: $os $arch")
    }
}

dependencies {
    // Elasticsearch plugin API - compileOnly since ES provides these at runtime
    compileOnly("org.elasticsearch.plugin:elasticsearch-plugin-api:$elasticsearchVersion")
    compileOnly("org.elasticsearch.plugin:elasticsearch-plugin-analysis-api:$elasticsearchVersion")

    // Lucene analysis - compileOnly since ES provides this
    compileOnly("org.apache.lucene:lucene-analysis-common:$luceneVersion")

    // KiwiJava - platform-specific JAR
    val platform = detectPlatform()
    implementation(files("libs/kiwi-java-v$kiwiJavaVersion-$platform.jar"))

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.apache.lucene:lucene-analysis-common:$luceneVersion")
    testImplementation("org.apache.lucene:lucene-test-framework:$luceneVersion")
    testImplementation("org.elasticsearch.plugin:elasticsearch-plugin-api:$elasticsearchVersion")
    testImplementation("org.elasticsearch.plugin:elasticsearch-plugin-analysis-api:$elasticsearchVersion")
}

// Java version based on ES version (ES 9.x requires Java 21)
val javaVersion = if (elasticsearchVersion.startsWith("9.")) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
val jvmTargetVersion = if (elasticsearchVersion.startsWith("9.")) "21" else "17"

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = jvmTargetVersion
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.test {
    useJUnitPlatform()
}

// Create stable plugin descriptor (no classname for stable plugins)
tasks.register("createPluginDescriptor") {
    val descriptorFile = layout.buildDirectory.file("stable-plugin-descriptor.properties")
    val namedComponentsFile = layout.buildDirectory.file("named_components.json")
    outputs.files(descriptorFile, namedComponentsFile)

    doLast {
        // Stable plugin descriptor
        val descriptor = descriptorFile.get().asFile
        descriptor.parentFile.mkdirs()
        descriptor.writeText("""
            description=Korean morphological analysis plugin using Kiwi
            version=${project.version}
            name=analysis-kiwi
            java.version=$jvmTargetVersion
            elasticsearch.version=$elasticsearchVersion
        """.trimIndent())

        // Named components mapping
        val namedComponents = namedComponentsFile.get().asFile
        namedComponents.writeText("""
            {
              "org.elasticsearch.plugin.analysis.TokenizerFactory": {
                "kiwi": "com.brandazine.elasticsearch.analysis.kiwi.KiwiTokenizerFactory"
              },
              "org.elasticsearch.plugin.analysis.TokenFilterFactory": {
                "kiwi_part_of_speech": "com.brandazine.elasticsearch.analysis.kiwi.KiwiPartOfSpeechFilterFactory"
              },
              "org.elasticsearch.plugin.analysis.AnalyzerFactory": {
                "kiwi": "com.brandazine.elasticsearch.analysis.kiwi.KiwiAnalyzerFactory"
              }
            }
        """.trimIndent())
    }
}

// Bundle plugin as ZIP
tasks.register<Zip>("bundlePlugin") {
    dependsOn("jar", "createPluginDescriptor")

    archiveBaseName.set("analysis-kiwi")
    archiveVersion.set("${project.version}-es${elasticsearchVersion}")
    archiveClassifier.set(detectPlatform())

    from(tasks.jar) {
        into("")
    }
    from(layout.buildDirectory.file("stable-plugin-descriptor.properties")) {
        into("")
    }
    from(layout.buildDirectory.file("named_components.json")) {
        into("")
    }
    from("src/main/plugin-metadata") {
        into("")
    }
    from(configurations.runtimeClasspath) {
        into("")
    }
}
