import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    `java-library`
}

group = "com.brandazine"
version = "1.0.0-SNAPSHOT"

val elasticsearchVersion = "8.12.0"
val luceneVersion = "9.9.1"

repositories {
    mavenCentral()
}

val kiwiJavaVersion = "0.22.2"

// Platform detection for KiwiJava JAR selection
fun detectPlatform(): String {
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
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.test {
    useJUnitPlatform()
}

// Create plugin descriptor
tasks.register("createPluginDescriptor") {
    val outputFile = layout.buildDirectory.file("plugin-descriptor.properties")
    outputs.file(outputFile)

    doLast {
        val descriptor = outputFile.get().asFile
        descriptor.parentFile.mkdirs()
        descriptor.writeText("""
            description=Korean morphological analysis plugin using Kiwi
            version=${project.version}
            name=analysis-kiwi
            classname=com.brandazine.elasticsearch.analysis.kiwi.KiwiAnalysisPlugin
            java.version=17
            elasticsearch.version=$elasticsearchVersion
            extended.plugins=
            has.native.controller=false
        """.trimIndent())
    }
}

// Bundle plugin as ZIP
tasks.register<Zip>("bundlePlugin") {
    dependsOn("jar", "createPluginDescriptor")

    archiveBaseName.set("analysis-kiwi")
    archiveClassifier.set(detectPlatform())

    from(tasks.jar) {
        into("")
    }
    from(layout.buildDirectory.file("plugin-descriptor.properties")) {
        into("")
    }
    from(configurations.runtimeClasspath) {
        into("")
        exclude("kotlin-stdlib*.jar") // ES provides Kotlin runtime
    }
}
