#!/bin/bash
#
# SPDX-License-Identifier: GPL-3.0-or-later
# Copyright (c) 2026. The LibreFit Contributors
#
# LibreFit is subject to additional terms covering author attribution and trademark usage;
# see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
#

set -e

# Config
INPUT_APK="repro-out/app-release-unsigned.apk"
ALIGNED_APK="repro-out/app-release-aligned.apk"
FINAL_APK="repro-out/app-release.apk"

# Secrets (passed via environment)
KEYSTORE_PATH="release.jks"
ALIAS="librefit_release"

if [ -z "$KEYSTORE_PASS" ]; then
    echo "❌ KEYSTORE_PASS env var is missing."
    exit 1
fi

echo "🔧 Aligning and Signing..."

if docker --version | grep -qi "podman"; then
    # Podman: Maps user automatically
    CONTAINER_ARGS="--userns=keep-id"
    PERMISSION_FIX=""
else
    # Docker: Runs as root. Chown the 'repro-out' folder at the end so files aren't locked as root on the host.
    CONTAINER_ARGS=""
    PERMISSION_FIX="chown -R $(id -u):$(id -g) repro-out"
fi

docker run --rm \
    $CONTAINER_ARGS \
    -v "$PWD":/project:z \
    -e KS_PASS="$KEYSTORE_PASS" \
    android-repro-check \
    /bin/bash -c "
        set -e
        export PATH=\$PATH:/opt/android-sdk/build-tools/36.0.0

        # Align to 16KB (Capital P is required for build tools 36)
        zipalign -f -P 16 -v 4 $INPUT_APK $ALIGNED_APK

        # Sign with Preservation: --alignment-preserved prevents apksigner from changing the padding bytes
        apksigner sign \
            --ks $KEYSTORE_PATH \
            --ks-key-alias $ALIAS \
            --ks-pass env:KS_PASS \
            --alignment-preserved \
            --out $FINAL_APK \
            $ALIGNED_APK

        # Verify
        apksigcopier compare $FINAL_APK --unsigned $INPUT_APK

        # Run permission fix if on standard Docker
        $PERMISSION_FIX
    "
echo "✅ Signed & Verified: $FINAL_APK"