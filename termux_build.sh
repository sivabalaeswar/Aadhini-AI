#!/bin/bash
# ============================================
# Aadhini AI — Termux Build Script
# Run this inside Termux on your Android phone
# ============================================

set -e
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'

echo -e "${BLUE}"
echo "  ⬡  AADHINI AI BUILD SCRIPT"
echo "  Personal AI Administrator"
echo -e "${NC}"

PROJECT_DIR="$HOME/AadhiniProject"

# ─── Step 1: Install dependencies ───────────────────────────────────────────
echo -e "${YELLOW}[1/5] Installing dependencies...${NC}"
pkg update -y -q
pkg install -y openjdk-17 wget unzip 2>/dev/null || true

# Verify Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}ERROR: Java not found. Run: pkg install openjdk-17${NC}"
    exit 1
fi
echo -e "${GREEN}  ✓ Java: $(java -version 2>&1 | head -1)${NC}"

# ─── Step 2: Install Gradle wrapper ─────────────────────────────────────────
echo -e "${YELLOW}[2/5] Setting up Gradle wrapper...${NC}"
cd "$PROJECT_DIR"

GRADLE_VERSION="8.2.2"
GRADLE_ZIP="gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_DIR="$HOME/.gradle-dist/gradle-${GRADLE_VERSION}"

if [ ! -d "$GRADLE_DIR" ]; then
    mkdir -p "$HOME/.gradle-dist"
    echo "  Downloading Gradle ${GRADLE_VERSION}..."
    wget -q --show-progress \
        "https://services.gradle.org/distributions/${GRADLE_ZIP}" \
        -O "$HOME/.gradle-dist/${GRADLE_ZIP}"
    unzip -q "$HOME/.gradle-dist/${GRADLE_ZIP}" -d "$HOME/.gradle-dist/"
    echo -e "${GREEN}  ✓ Gradle downloaded${NC}"
else
    echo -e "${GREEN}  ✓ Gradle already installed${NC}"
fi

GRADLE_BIN="$GRADLE_DIR/bin/gradle"
chmod +x "$GRADLE_BIN"

# ─── Step 3: Setup Android SDK (sdkmanager) ──────────────────────────────────
echo -e "${YELLOW}[3/5] Checking Android SDK...${NC}"

ANDROID_SDK="$HOME/android-sdk"
if [ ! -d "$ANDROID_SDK/build-tools" ]; then
    echo "  Android SDK not found. Setting up command line tools..."
    mkdir -p "$ANDROID_SDK/cmdline-tools"
    
    CMDTOOLS_ZIP="commandlinetools-linux-10406996_latest.zip"
    if [ ! -f "$HOME/$CMDTOOLS_ZIP" ]; then
        wget -q --show-progress \
            "https://dl.google.com/android/repository/$CMDTOOLS_ZIP" \
            -O "$HOME/$CMDTOOLS_ZIP"
    fi
    
    unzip -q "$HOME/$CMDTOOLS_ZIP" -d "$ANDROID_SDK/cmdline-tools/"
    mv "$ANDROID_SDK/cmdline-tools/cmdline-tools" "$ANDROID_SDK/cmdline-tools/latest" 2>/dev/null || true
    
    export ANDROID_HOME="$ANDROID_SDK"
    export PATH="$PATH:$ANDROID_SDK/cmdline-tools/latest/bin:$ANDROID_SDK/platform-tools"
    
    echo "  Accepting licenses..."
    yes | sdkmanager --licenses > /dev/null 2>&1 || true
    
    echo "  Installing build tools (this takes a few minutes)..."
    sdkmanager "platforms;android-34" "build-tools;34.0.0" > /dev/null 2>&1
    echo -e "${GREEN}  ✓ Android SDK installed${NC}"
else
    export ANDROID_HOME="$ANDROID_SDK"
    export PATH="$PATH:$ANDROID_SDK/cmdline-tools/latest/bin:$ANDROID_SDK/platform-tools"
    echo -e "${GREEN}  ✓ Android SDK found${NC}"
fi

# Write local.properties
echo "sdk.dir=$ANDROID_SDK" > "$PROJECT_DIR/local.properties"
echo -e "${GREEN}  ✓ local.properties written${NC}"

# ─── Step 4: Build APK ───────────────────────────────────────────────────────
echo -e "${YELLOW}[4/5] Building Aadhini APK...${NC}"
cd "$PROJECT_DIR"

export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export ANDROID_HOME="$ANDROID_SDK"

"$GRADLE_BIN" \
    --no-daemon \
    --stacktrace \
    :aadhini-android:assembleDebug \
    2>&1 | tail -30

# ─── Step 5: Copy APK ────────────────────────────────────────────────────────
echo -e "${YELLOW}[5/5] Locating APK...${NC}"

APK_PATH=$(find "$PROJECT_DIR" -name "*.apk" 2>/dev/null | head -1)

if [ -n "$APK_PATH" ]; then
    DEST="$HOME/storage/downloads/Aadhini-v1.0-debug.apk"
    mkdir -p "$HOME/storage/downloads" 2>/dev/null || true
    cp "$APK_PATH" "$DEST" 2>/dev/null || cp "$APK_PATH" "$HOME/Aadhini-v1.0-debug.apk"
    
    echo -e "${GREEN}"
    echo "  ╔══════════════════════════════════════╗"
    echo "  ║   ✅  BUILD SUCCESSFUL!              ║"
    echo "  ║                                      ║"
    echo "  ║   APK: Aadhini-v1.0-debug.apk        ║"
    echo "  ║   Location: ~/storage/downloads/     ║"
    echo "  ║                                      ║"
    echo "  ║   Install: Open file manager →       ║"
    echo "  ║   Downloads → tap APK to install    ║"
    echo "  ╚══════════════════════════════════════╝"
    echo -e "${NC}"
else
    echo -e "${RED}  ✗ APK not found. Check errors above.${NC}"
    exit 1
fi
