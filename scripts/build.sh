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


if docker --version | grep -qi "podman"; then
    echo "  > Detected Engine: Podman"
    # Podman: Maps host user to container user automatically
    CONTAINER_ARGS="--userns=keep-id"
    # No manual permission fix needed
    PERMISSION_FIX=""
else
    echo "  > Detected Engine: Docker"
    # Docker: It runs as root (default). Set GRADLE_USER_HOME to the mounted volume so cache persists.
    CONTAINER_ARGS="-e GRADLE_USER_HOME=/project/.gradle"

    # Docker workaround: Since it ran as root, the files are now owned by root.
    # Run 'chown' inside the container before exiting to give ownership back to the host user.
    PERMISSION_FIX="&& chown -R $(id -u):$(id -g) /project"
fi

# Docker command with fix for SELinux (:z) and permissions (chmod)
docker run --rm \
    "$CONTAINER_ARGS" \
    -v "$PWD":/project:z \
    android-repro-check \
    /bin/bash -c "chmod +x gradlew && ./gradlew clean assembleRelease --no-daemon $PERMISSION_FIX"


if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "$OUTPUT_DIR/app-release-unsigned.apk"
    echo "✅ Build Successful: $OUTPUT_DIR/app-release-unsigned.apk"
    # Print Hash for logs
    sha256sum "$OUTPUT_DIR/app-release-unsigned.apk"
else
    echo "❌ Build Failed: APK not found."
    exit 1
fi