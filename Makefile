# Kiwi Analyzer Plugin - Makefile
# Automatically downloads KiwiJava and model files from latest GitHub release

SHELL := /bin/bash

# GitHub API URL
GITHUB_API := https://api.github.com/repos/bab2min/Kiwi/releases/latest

# Detect platform
UNAME_S := $(shell uname -s)
UNAME_M := $(shell uname -m)

ifeq ($(UNAME_S),Darwin)
    ifeq ($(UNAME_M),arm64)
        PLATFORM := mac-arm64
    else
        PLATFORM := mac-x86_64
    endif
else ifeq ($(UNAME_S),Linux)
    ifeq ($(UNAME_M),aarch64)
        PLATFORM := lnx-aarch64
    else
        PLATFORM := lnx-x86-64
    endif
else
    PLATFORM := win-x64
endif

# Directories
LIBS_DIR := libs
MODEL_DIR := kiwi

.PHONY: all clean setup download-kiwi download-model test build info

all: setup build

info:
	@echo "Platform detected: $(PLATFORM)"
	@echo "Fetching latest release info..."
	@curl -s $(GITHUB_API) | jq -r '.tag_name, .name'

setup: download-kiwi download-model
	@echo "Setup complete!"

download-kiwi: $(LIBS_DIR)
	@echo "Fetching latest KiwiJava JAR for $(PLATFORM)..."
	@DOWNLOAD_URL=$$(curl -s $(GITHUB_API) | jq -r '.assets[] | select(.name | contains("kiwi-java") and contains("$(PLATFORM)")) | .browser_download_url'); \
	if [ -z "$$DOWNLOAD_URL" ]; then \
		echo "Error: Could not find KiwiJava JAR for platform $(PLATFORM)"; \
		exit 1; \
	fi; \
	FILENAME=$$(basename "$$DOWNLOAD_URL"); \
	echo "Downloading $$FILENAME..."; \
	curl -L --progress-bar -o $(LIBS_DIR)/$$FILENAME "$$DOWNLOAD_URL"; \
	echo "Downloaded: $(LIBS_DIR)/$$FILENAME"

download-model: $(MODEL_DIR)
	@echo "Fetching latest Kiwi model..."
	@DOWNLOAD_URL=$$(curl -s $(GITHUB_API) | jq -r '.assets[] | select(.name | contains("kiwi_model") and contains("base.tgz")) | .browser_download_url'); \
	if [ -z "$$DOWNLOAD_URL" ]; then \
		echo "Error: Could not find Kiwi model"; \
		exit 1; \
	fi; \
	FILENAME=$$(basename "$$DOWNLOAD_URL"); \
	echo "Downloading $$FILENAME..."; \
	curl -L --progress-bar -o /tmp/$$FILENAME "$$DOWNLOAD_URL"; \
	echo "Extracting model..."; \
	tar -xzf /tmp/$$FILENAME -C $(MODEL_DIR); \
	rm /tmp/$$FILENAME; \
	echo "Model extracted to $(MODEL_DIR)/"

$(LIBS_DIR):
	mkdir -p $(LIBS_DIR)

$(MODEL_DIR):
	mkdir -p $(MODEL_DIR)

# Update build.gradle.kts to use the downloaded JAR version
update-gradle:
	@echo "Detecting downloaded KiwiJava version..."
	@JAR_FILE=$$(ls -1 $(LIBS_DIR)/kiwi-java-*.jar 2>/dev/null | head -1); \
	if [ -z "$$JAR_FILE" ]; then \
		echo "Error: No KiwiJava JAR found in $(LIBS_DIR)/"; \
		exit 1; \
	fi; \
	VERSION=$$(basename "$$JAR_FILE" | sed -E 's/kiwi-java-v([0-9.]+)-.*/\1/'); \
	echo "Found version: $$VERSION"; \
	sed -i.bak "s/val kiwiJavaVersion = \"[^\"]*\"/val kiwiJavaVersion = \"$$VERSION\"/" build.gradle.kts; \
	rm -f build.gradle.kts.bak; \
	echo "Updated build.gradle.kts with version $$VERSION"

# Find model path (handles different directory structures)
find-model-path:
	@MODEL_PATH=$$(find $(MODEL_DIR) -name "*.knlm" -o -name "*.morph" 2>/dev/null | head -1 | xargs dirname 2>/dev/null); \
	if [ -z "$$MODEL_PATH" ]; then \
		echo "Error: Could not find model files in $(MODEL_DIR)/"; \
		exit 1; \
	fi; \
	echo "$$MODEL_PATH"

test: setup update-gradle
	@MODEL_PATH=$$(make -s find-model-path); \
	echo "Running tests with model path: $$MODEL_PATH"; \
	KIWI_MODEL_PATH="$$MODEL_PATH" ./gradlew test

build: setup update-gradle
	./gradlew build bundlePlugin

clean:
	./gradlew clean
	rm -rf $(LIBS_DIR)/*.jar
	rm -rf $(MODEL_DIR)/*

clean-all: clean
	rm -rf $(LIBS_DIR)
	rm -rf $(MODEL_DIR)

# Show available releases
list-releases:
	@echo "Recent Kiwi releases:"
	@curl -s https://api.github.com/repos/bab2min/Kiwi/releases | jq -r '.[0:5] | .[] | "\(.tag_name) - \(.published_at)"'

# Download specific version
download-version:
	@if [ -z "$(VERSION)" ]; then \
		echo "Usage: make download-version VERSION=v0.22.2"; \
		exit 1; \
	fi; \
	echo "Downloading version $(VERSION)..."; \
	RELEASE_URL="https://api.github.com/repos/bab2min/Kiwi/releases/tags/$(VERSION)"; \
	JAR_URL=$$(curl -s "$$RELEASE_URL" | jq -r '.assets[] | select(.name | contains("kiwi-java") and contains("$(PLATFORM)")) | .browser_download_url'); \
	MODEL_URL=$$(curl -s "$$RELEASE_URL" | jq -r '.assets[] | select(.name | contains("kiwi_model") and contains("base.tgz")) | .browser_download_url'); \
	if [ -n "$$JAR_URL" ]; then \
		echo "Downloading JAR: $$JAR_URL"; \
		curl -L --progress-bar -o $(LIBS_DIR)/$$(basename "$$JAR_URL") "$$JAR_URL"; \
	fi; \
	if [ -n "$$MODEL_URL" ]; then \
		echo "Downloading model: $$MODEL_URL"; \
		curl -L --progress-bar -o /tmp/model.tgz "$$MODEL_URL"; \
		tar -xzf /tmp/model.tgz -C $(MODEL_DIR); \
		rm /tmp/model.tgz; \
	fi

help:
	@echo "Kiwi Analyzer Plugin - Makefile"
	@echo ""
	@echo "Usage:"
	@echo "  make setup          - Download KiwiJava JAR and model (latest)"
	@echo "  make download-kiwi  - Download only KiwiJava JAR"
	@echo "  make download-model - Download only Kiwi model"
	@echo "  make update-gradle  - Update build.gradle.kts with downloaded version"
	@echo "  make test           - Run tests with downloaded model"
	@echo "  make build          - Build plugin"
	@echo "  make clean          - Clean build and downloaded files"
	@echo "  make info           - Show detected platform and latest release"
	@echo "  make list-releases  - List recent releases"
	@echo "  make download-version VERSION=v0.22.2 - Download specific version"
	@echo ""
	@echo "Detected platform: $(PLATFORM)"
