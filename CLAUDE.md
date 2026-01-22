# Kiwi Analyzer - Elasticsearch Analysis Plugin

## Project Overview

Korean morphological analysis plugin for Elasticsearch using [Kiwi](https://github.com/bab2min/Kiwi) library. Written in Kotlin, supporting Elasticsearch 8.x and 9.x.

## Quick Commands

```bash
# Setup (downloads KiwiJava + model)
make setup

# Run tests
make test

# Build plugin (default: ES 8.12.0)
make build

# Build for specific ES version
ES_VERSION=9.2.2 ./gradlew bundlePlugin

# Clean
make clean
```

## Project Structure

```
kiwi-analyzer/
├── build.gradle.kts         # Gradle build config (Kotlin DSL)
├── Makefile                  # Setup automation (downloads KiwiJava + model)
├── Dockerfile.test           # Docker test setup for ES 8.x/9.x
├── libs/                     # KiwiJava platform-specific JARs
├── kiwi/                     # Kiwi model files (downloaded by make setup)
└── src/
    ├── main/kotlin/.../kiwi/
    │   ├── KiwiAnalysisPlugin.kt      # Main plugin class (extends AnalysisPlugin)
    │   ├── KiwiTokenizer.kt           # Lucene Tokenizer implementation
    │   ├── KiwiTokenizerFactory.kt    # Tokenizer factory (implements TokenizerFactory)
    │   ├── KiwiAnalyzer.kt            # Lucene Analyzer
    │   ├── KiwiAnalyzerFactory.kt     # Analyzer provider (implements AnalyzerProvider)
    │   ├── KiwiPartOfSpeechFilter.kt  # POS tag token filter
    │   ├── KiwiPartOfSpeechFilterFactory.kt
    │   ├── KiwiInstanceManager.kt     # Singleton Kiwi instance manager
    │   ├── NativeLibraryLoader.kt     # JNI native library loading (with doPrivileged)
    │   └── POSTagSet.kt               # Korean POS tag constants
    ├── main/plugin-metadata/
    │   ├── entitlement-policy.yaml    # ES 9.x entitlements
    │   └── plugin-security.policy     # ES 8.x security policy
    └── test/kotlin/.../kiwi/
        ├── KiwiTokenizerIntegrationTest.kt
        ├── KiwiAnalyzerIntegrationTest.kt
        ├── KiwiLoadingTest.kt
        ├── POSTagSetTest.kt
        └── ClassicPluginApiTest.kt
```

## Key Components

### Plugin Entry Point
- `KiwiAnalysisPlugin.kt` - Main plugin class extending `Plugin` and implementing `AnalysisPlugin`
  - Registers tokenizers via `getTokenizers()`
  - Registers filters via `getTokenFilters()`
  - Registers analyzers via `getAnalyzers()`

### Tokenizer (`type: "kiwi"`)
- `KiwiTokenizer.kt` - Core implementation that converts Kiwi tokens to Lucene tokens
- `KiwiTokenizerFactory.kt` - Implements `TokenizerFactory`, parses settings from `Settings` object

### Token Filter (`type: "kiwi_part_of_speech"`)
- `KiwiPartOfSpeechFilter.kt` - Filters tokens by POS tag
- `KiwiPartOfSpeechFilterFactory.kt` - Implements `TokenFilterFactory`

### Analyzer (`type: "kiwi"`)
- `KiwiAnalyzer.kt` - Complete analyzer combining tokenizer + POS filter + lowercase
- `KiwiAnalyzerFactory.kt` - Implements `AnalyzerProvider`

### Infrastructure
- `KiwiInstanceManager.kt` - Manages Kiwi instances (singleton per config)
- `NativeLibraryLoader.kt` - Handles JNI native library loading with `AccessController.doPrivileged`
- `POSTagSet.kt` - Korean POS tag constants (NNG, NNP, VV, etc.)

## Configuration Options

### Tokenizer Settings
| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `model_path` | string | `"kiwi"` | Kiwi model directory path |
| `num_threads` | int | `0` | Worker threads (0=auto) |
| `discard_punctuation` | bool | `true` | Remove punctuation |
| `user_dictionary` | string | `""` | User dictionary file path |
| `pos_tags_to_include` | list | `[]` | POS tags to include (empty=all) |

### POS Filter Settings
| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `stop_tags` | list | `[]` | POS tags to filter out |

## Testing

Tests require:
1. KiwiJava JAR in `libs/`
2. Kiwi model in `kiwi/`
3. Environment variable `KIWI_MODEL_PATH` pointing to model files

```bash
# Automated (handles setup + env var)
make test

# Manual
KIWI_MODEL_PATH=$(pwd)/kiwi/base ./gradlew test
```

## Build Output

Plugin ZIP: `build/distributions/analysis-kiwi-1.0.0-SNAPSHOT-es{version}-{platform}.zip`

Examples:
- `analysis-kiwi-1.0.0-SNAPSHOT-es8.12.0-lnx-x86-64.zip`
- `analysis-kiwi-1.0.0-SNAPSHOT-es9.2.2-lnx-x86-64.zip`

Platforms: `lnx-x86-64`, `lnx-aarch64`, `mac-arm64`, `win-x64`

## ES Classic Plugin API Notes

- Uses Classic Plugin API (not Stable API) for ES 8.x/9.x compatibility
- `KiwiAnalysisPlugin.kt` is the main entry point (`classname` in plugin-descriptor.properties)
- Factory classes implement interfaces directly (`TokenizerFactory`, `TokenFilterFactory`, `AnalyzerProvider`)
- Settings are parsed manually from `Settings` object (no `@AnalysisSettings` interfaces)
- Security policies:
  - ES 8.x: `plugin-security.policy` (SecurityManager)
  - ES 9.x: `entitlement-policy.yaml` (Entitlements)
- Native library loading uses `AccessController.doPrivileged` for security compliance

## Docker Testing

```bash
# Build and test on ES 8.12.0 (x86-64)
docker build --platform linux/amd64 --build-arg ES_VERSION=8.12.0 -f Dockerfile.test -t kiwi-es8-test .
docker run -d --name kiwi-es8 --platform linux/amd64 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -p 9200:9200 kiwi-es8-test

# Build and test on ES 9.2.2 (ARM64 for Apple Silicon)
KIWI_PLATFORM=lnx-aarch64 ES_VERSION=9.2.2 ./gradlew clean bundlePlugin -x test
docker build --platform linux/arm64 --build-arg ES_VERSION=9.2.2 -f Dockerfile.test -t kiwi-es9-test .
docker run -d --name kiwi-es9 --platform linux/arm64 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -p 9201:9200 kiwi-es9-test

# Test
curl -X POST "localhost:9200/_analyze" -H "Content-Type: application/json" \
  -d '{"tokenizer": {"type": "kiwi", "model_path": "/usr/share/elasticsearch/config/kiwi"}, "text": "안녕하세요"}'
```

## Dependencies

| ES Version | Java | Lucene |
|------------|------|--------|
| 8.x | 17 | 9.9.1 |
| 9.x | 21 | 10.1.0 |

- KiwiJava (runtime, platform-specific)
- Kotlin 1.9.22
