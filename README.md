# Kiwi Analyzer - Elasticsearch Korean Analysis Plugin

Elasticsearch 한국어 형태소 분석 플러그인. [Kiwi](https://github.com/bab2min/Kiwi) 형태소 분석기를 기반으로 합니다.

## Features

- **고성능 형태소 분석**: Kiwi 라이브러리 기반 빠른 처리
- **사용자 정의 사전**: 커스텀 단어/품사 추가 지원
- **품사 필터링**: 특정 품사만 포함하거나 제외
- **멀티 플랫폼**: Linux, macOS (Intel/Apple Silicon), Windows 지원
- **Elasticsearch 8.12+ 호환**: Stable Plugin API 사용

## Installation

### 1. Prerequisites

- Elasticsearch 8.12.0+
- Java 17+
- `curl`, `jq`, `tar` (for setup)

### 2. Build the Plugin

```bash
# Clone the repository
git clone https://github.com/brandazine/kiwi-analyzer.git
cd kiwi-analyzer

# Setup (downloads KiwiJava and model files)
make setup

# Build plugin
make build
```

빌드 결과물: `build/distributions/analysis-kiwi-1.0.0-SNAPSHOT-{platform}.zip`

### 3. Install to Elasticsearch

```bash
# Install plugin
bin/elasticsearch-plugin install file:///path/to/analysis-kiwi-1.0.0-SNAPSHOT-{platform}.zip

# Copy model files to ES config directory
cp -r kiwi/base /etc/elasticsearch/kiwi

# Restart Elasticsearch
systemctl restart elasticsearch
```

## Usage

### Basic Analyzer

가장 간단한 사용법 - 내장 `kiwi` 분석기 사용:

```json
PUT /korean_index
{
  "settings": {
    "analysis": {
      "analyzer": {
        "korean": {
          "type": "kiwi",
          "model_path": "/etc/elasticsearch/kiwi"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "analyzer": "korean"
      }
    }
  }
}
```

### Custom Tokenizer with Filters

토크나이저와 필터를 개별 설정:

```json
PUT /korean_index
{
  "settings": {
    "analysis": {
      "tokenizer": {
        "kiwi_tokenizer": {
          "type": "kiwi",
          "model_path": "/etc/elasticsearch/kiwi",
          "discard_punctuation": true
        }
      },
      "filter": {
        "pos_filter": {
          "type": "kiwi_part_of_speech",
          "stop_tags": ["JKS", "JKO", "JKB", "JKV", "JKQ", "JX", "JC", "EP", "EF", "EC", "ETN", "ETM"]
        }
      },
      "analyzer": {
        "korean_custom": {
          "type": "custom",
          "tokenizer": "kiwi_tokenizer",
          "filter": ["pos_filter", "lowercase"]
        }
      }
    }
  }
}
```

### User Dictionary

사용자 정의 사전으로 고유명사, 신조어 등 추가:

**1. 사전 파일 생성** (`/etc/elasticsearch/kiwi/user_dict.txt`):

```
# 형식: 단어<TAB>품사<TAB>점수 (점수 생략 가능)
브랜다진	NNP	0.0
딥러닝	NNG	0.0
ChatGPT	SL	0.0
삼성전자	NNP	0.0
```

**2. 설정에서 사전 경로 지정**:

```json
{
  "tokenizer": {
    "kiwi_tokenizer": {
      "type": "kiwi",
      "model_path": "/etc/elasticsearch/kiwi",
      "user_dictionary": "/etc/elasticsearch/kiwi/user_dict.txt"
    }
  }
}
```

### Test with _analyze API

```bash
POST /_analyze
{
  "analyzer": "korean",
  "text": "안녕하세요 한국어 형태소 분석입니다"
}
```

결과:
```json
{
  "tokens": [
    {"token": "안녕", "type": "NNG", ...},
    {"token": "하", "type": "XSA", ...},
    {"token": "세요", "type": "EF", ...},
    {"token": "한국어", "type": "NNG", ...},
    {"token": "형태소", "type": "NNG", ...},
    {"token": "분석", "type": "NNG", ...},
    {"token": "이", "type": "VCP", ...}
  ]
}
```

## Configuration Reference

### Tokenizer (`type: "kiwi"`)

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `model_path` | string | `"kiwi"` | Kiwi 모델 디렉토리 경로 |
| `num_threads` | int | `0` | 워커 스레드 수 (0 = 자동 감지) |
| `discard_punctuation` | bool | `true` | 구두점 토큰 제거 |
| `user_dictionary` | string | `""` | 사용자 정의 사전 파일 경로 |
| `pos_tags_to_include` | list | `[]` | 포함할 품사 태그 (빈 리스트 = 전체 포함) |

### Token Filter (`type: "kiwi_part_of_speech"`)

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `stop_tags` | list | `[]` | 제거할 품사 태그 |

### Analyzer (`type: "kiwi"`)

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `model_path` | string | `"kiwi"` | Kiwi 모델 디렉토리 경로 |
| `num_threads` | int | `0` | 워커 스레드 수 |
| `discard_punctuation` | bool | `true` | 구두점 제거 |
| `user_dictionary` | string | `""` | 사용자 정의 사전 경로 |
| `stop_tags` | list | `[]` | 제거할 품사 태그 |

## Korean POS Tags

주요 품사 태그:

| Tag | Description | Example |
|-----|-------------|---------|
| NNG | 일반 명사 | 학교, 사람 |
| NNP | 고유 명사 | 서울, 삼성 |
| NNB | 의존 명사 | 것, 수 |
| NR | 수사 | 하나, 둘 |
| NP | 대명사 | 나, 너 |
| VV | 동사 | 가다, 먹다 |
| VA | 형용사 | 크다, 작다 |
| VX | 보조 용언 | 하다, 되다 |
| VCP | 긍정 지정사 | 이다 |
| VCN | 부정 지정사 | 아니다 |
| MM | 관형사 | 이, 그, 저 |
| MAG | 일반 부사 | 매우, 빨리 |
| MAJ | 접속 부사 | 그러나, 하지만 |
| IC | 감탄사 | 아, 와 |
| JKS | 주격 조사 | 이/가 |
| JKC | 보격 조사 | 이/가 |
| JKG | 관형격 조사 | 의 |
| JKO | 목적격 조사 | 을/를 |
| JKB | 부사격 조사 | 에, 에서 |
| JKV | 호격 조사 | 아/야 |
| JKQ | 인용격 조사 | 라고 |
| JX | 보조사 | 은/는, 도 |
| JC | 접속 조사 | 와/과 |
| EP | 선어말 어미 | 시, 었 |
| EF | 종결 어미 | 다, 요 |
| EC | 연결 어미 | 고, 며 |
| ETN | 명사형 전성 어미 | 기, 음 |
| ETM | 관형형 전성 어미 | 는, 은 |
| XPN | 체언 접두사 | 풋, 첫 |
| XSN | 명사 파생 접미사 | 님, 적 |
| XSV | 동사 파생 접미사 | 하, 되 |
| XSA | 형용사 파생 접미사 | 스럽, 답 |
| XR | 어근 | - |
| SF | 마침표/물음표/느낌표 | . ? ! |
| SP | 쉼표/가운뎃점/콜론/빗금 | , · : / |
| SS | 따옴표/괄호/줄표 | " ' ( ) - |
| SE | 줄임표 | … |
| SO | 붙임표 | ~ |
| SW | 기타 기호 | - |
| SL | 외국어 | hello |
| SH | 한자 | 漢字 |
| SN | 숫자 | 123 |

## Development

### Requirements

- JDK 17+
- Gradle 8.x (wrapper included)
- `make`, `curl`, `jq`

### Build Commands

```bash
make setup        # Download KiwiJava + model
make test         # Run tests
make build        # Build plugin ZIP
make clean        # Clean build artifacts
make clean-all    # Clean everything including downloads
make info         # Show platform and latest release info
```

### Running Tests

```bash
# Full test with setup
make test

# Manual (requires KIWI_MODEL_PATH)
KIWI_MODEL_PATH=$(pwd)/kiwi/base ./gradlew test
```

### Download Specific Version

```bash
make download-version VERSION=v0.22.2
```

## Supported Platforms

| Platform | JAR Suffix | Build Artifact |
|----------|------------|----------------|
| Linux x64 | `lnx-x86-64` | `analysis-kiwi-*-lnx-x86-64.zip` |
| Linux ARM64 | `lnx-aarch64` | `analysis-kiwi-*-lnx-aarch64.zip` |
| macOS Apple Silicon | `mac-arm64` | `analysis-kiwi-*-mac-arm64.zip` |
| Windows x64 | `win-x64` | `analysis-kiwi-*-win-x64.zip` |

## License

This project is licensed under the MIT License.

Kiwi library is licensed under LGPL v3. See [Kiwi repository](https://github.com/bab2min/Kiwi) for details.

## Credits

- [Kiwi](https://github.com/bab2min/Kiwi) - Korean Intelligent Word Identifier by bab2min
- [KiwiJava](https://github.com/bab2min/Kiwi) - Java bindings for Kiwi
