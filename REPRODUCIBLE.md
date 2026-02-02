# Reproducible Builds & Verification

This project supports [Reproducible Builds](https://reproducible-builds.org/).
This means you can verify that the APK distributed on GitHub or F-Droid matches the source code exactly, proving no hidden code was injected during the release process.

## Build Environment
To ensure determinism, we use a containerized build environment.

*   **OS:** Ubuntu 22.04 (Pinned Digest)
*   **Android Gradle Plugin:** 9.0+
*   **Build Tools:** 36.0.0
*   **Alignment:** 16KB (Android 15 Compliant)

## Prerequisites
*   Docker or Podman
*   Git
*   Python 3 (for verifying signed releases)

---

## 1. How to Build from Source
To reproduce the **Unsigned APK** exactly as it was built by the CI server:

1.  **Clone the repository and checkout the tag/commit you want to verify:**
    ```bash
    git clone https://github.com/LibreFitOrg/LibreFit.git
    cd LibreFit
    git checkout v1.0.0
    ```

2.  **Build the Docker Image:**
    ```bash
    docker build -t android-repro-check -f Dockerfile.build .
    ```

3.  **Run the Build Script:**
    ```bash
    chmod +x scripts/build.sh
    ./scripts/build.sh
    ```

4.  **Check the Output:**
    The artifact will be at `repro-out/app-release-unsigned.apk`.
    The script will output the **SHA-256 hash**.

---

## 2. How to Verify a Signed Release
Because this app targets Android 15 (Build Tools 36+), it requires specific **16KB page alignment**. Standard tools (like `unzip` or `sha256sum`) cannot directly compare the Signed APK to the Source Code because the signing process alters the binary padding.

Use `apksigcopier` instead to compare the APKs.

1.  **Install Verification Tool:**
    ```bash
    pip3 install apksigcopier
    ```

2.  **Download the Release:**
    Download `app-release.apk` from the GitHub Releases page.

3.  **Run Comparison:**
    Compare the official release against your local build:
    ```bash
    apksigcopier compare app-release-signed.apk --unsigned repro-out/app-release-unsigned.apk
    ```

### Interpreting Results
*   **Exit Code 0 (No Output):** ✅ **SUCCESS.** The signed APK contains *exactly* the same compiled code, resources, and assets as the source code.
*   **Exit Code 1 (Failure):** ❌ **MISMATCH.** The binary content differs. Open an issue if this error persists.

---

## Technical Details for F-Droid / Auditors
*   **Root Directory:** `/project`
*   **Build Command:** `./gradlew clean assembleRelease --no-daemon`
*   **Environment Variables:**
    *   `SOURCE_DATE_EPOCH=1700000000` (Fixes ZIP timestamps)
    *   `GRADLE_OPTS=-Dorg.gradle.workers.max=1` (Ensures deterministic R8/DEX generation)