#!/bin/bash
#
# SPDX-License-Identifier: GPL-3.0-or-later
# Copyright (c) 2026. The LibreFit Contributors
#
# LibreFit is subject to additional terms covering author attribution and trademark usage;
# see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
#

set -e

# Output config
OUTPUT_DIR="repro-out"
APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
rm -rf "$OUTPUT_DIR" && mkdir -p "$OUTPUT_DIR"

echo "🏗️  Building Unsigned APK..."

# Docker command with fix for SELinux (:z) and permissions (chmod)
docker run --rm \
    --userns=keep-id \
    -v "$PWD":/project:z \
    android-repro-check \
    /bin/bash -c "chmod +x gradlew && ./gradlew clean assembleRelease --no-daemon"

if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "$OUTPUT_DIR/app-release-unsigned.apk"
    echo "✅ Build Successful: $OUTPUT_DIR/app-release-unsigned.apk"
    # Print Hash for logs
    sha256sum "$OUTPUT_DIR/app-release-unsigned.apk"
else
    echo "❌ Build Failed: APK not found."
    exit 1
fi