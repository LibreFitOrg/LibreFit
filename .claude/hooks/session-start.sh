#!/bin/bash
# Installs the Android SDK + cmdline-tools needed to run `./gradlew lintDebug`
# and `./gradlew testDebugUnitTest` in the remote Claude Code session.
#
# Only runs in the remote environment.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
CMDLINE_TOOLS_VERSION="11076708"   # commandlinetools-linux-11076708_latest.zip
PLATFORM_API="37.0"
BUILD_TOOLS="37.0.0"

mkdir -p "$ANDROID_SDK_ROOT"

if [ ! -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "Installing Android cmdline-tools…"
  TMP_ZIP="$(mktemp -d)/cmdline-tools.zip"
  curl -fsSL -o "$TMP_ZIP" \
    "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
  rm -rf "$ANDROID_SDK_ROOT/cmdline-tools"
  mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
  unzip -q "$TMP_ZIP" -d "$ANDROID_SDK_ROOT/cmdline-tools"
  mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
  rm -f "$TMP_ZIP"
fi

export ANDROID_SDK_ROOT
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

yes 2>/dev/null | sdkmanager --licenses >/dev/null || true

sdkmanager --install \
  "platforms;android-${PLATFORM_API}" \
  "build-tools;${BUILD_TOOLS}" \
  "platform-tools" >/dev/null

# Persist for the rest of the session.
{
  echo "export ANDROID_SDK_ROOT=\"$ANDROID_SDK_ROOT\""
  echo "export ANDROID_HOME=\"$ANDROID_SDK_ROOT\""
  echo "export PATH=\"$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:\$PATH\""
} >> "$CLAUDE_ENV_FILE"

echo "Android SDK ready at $ANDROID_SDK_ROOT"
