#!/bin/bash
set -e

echo "⚙️  Setting up environment (no sudo)..."

# === Java (OpenJDK 17) ===
export JAVA_HOME=$HOME/java/jdk-17.0.2
export PATH=$JAVA_HOME/bin:$PATH

if [ ! -d "$JAVA_HOME" ]; then
  mkdir -p $HOME/java
  cd $HOME/java
  wget -q https://download.java.net/java/GA/jdk17.0.2/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz
  tar -xzf openjdk-17.0.2_linux-x64_bin.tar.gz
  rm openjdk-17.0.2_linux-x64_bin.tar.gz
fi

java -version

# === Android SDK ===
export ANDROID_SDK_ROOT=$HOME/android-sdk
mkdir -p $ANDROID_SDK_ROOT/cmdline-tools
cd $ANDROID_SDK_ROOT/cmdline-tools

if [ ! -d "$ANDROID_SDK_ROOT/cmdline-tools/latest" ]; then
  echo "📦 Downloading Android command-line tools..."
  wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O tools.zip
  unzip -q tools.zip -d latest
  rm tools.zip
fi

export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# === Build App ===
cd /workspaces/MiniBrowser_v6
chmod +x gradlew
./gradlew assembleDebug || ./gradlew build

echo "✅ APK ready at app/build/outputs/apk/debug/app-debug.apk"
