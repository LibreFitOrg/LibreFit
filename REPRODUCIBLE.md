# Reproducible Builds

This project supports [Reproducible Builds](https://reproducible-builds.org/).
This means the APK distributed on GitHub or F-Droid matches the source code exactly, proving no hidden code was injected during the release process.

> [!TIP]
> More info at: https://f-droid.org/docs/Reproducible_Builds/

## Verification Guide

This document provides a step-by-step guide to verifying that a locally built, unsigned APK perfectly matches the release APK.

While tools like `apksigcopier` are the standard for this process, changes in modern Android Build Tools (36.0.0+) and Python environments can
sometimes cause [signature-copying utilities to fail](https://f-droid.org/docs/Reproducible_Builds/).

In these cases, `diffoscope` is the recommended fallback. It performs a deep, recursive comparison of the APKs to ensure that the compiled source
code, resources, and native libraries are entirely identical.

### How Verification Works

An APK is essentially a ZIP archive. When a developer signs an APK, the signing tool (`apksigner`) injects two things into the unsigned APK:

1. **V1 Signature Files:** Three files added to the `META-INF/` directory (`MANIFEST.MF`, `*.SF`, and `*.RSA`/`*.DSA`).
2. **V2/V3 Signing Block:** A binary block inserted into the ZIP archive structure itself.

If a build is perfectly reproducible, every single file compiled from the source code (`classes.dex`, `resources.arsc`, `AndroidManifest.xml`, native
libraries, etc.) will be byte-for-byte identical to the developer's build before signing. Therefore, when comparing a signed APK to an unsigned APK,
the **only** differences that should exist are those injected signature files. If `diffoscope` confirms this, reproducibility is mathematically
verified.

### Prerequisites
- Git
- JDK 21 (Eclipse Temurin recommended)
- Python 3.10+ and `python3-venv`
- Android SDK (required if building locally)

### Step-by-Step Guide

#### Step 1: Set up the Python Virtual Environment

To ensure a clean environment, `diffoscope` should be run inside an isolated Python virtual environment.

1. Create a new virtual environment in the project directory:
   ```shell
   python3 -m venv .venv
   ```
2. Activate the virtual environment:
   ```shell
   source ./.venv/bin/activate    # For MacOS/Linux
   .\.venv\Scripts\Activate.ps1   # Windows (PowerShell)
   ```
3. Install `diffoscope` via pip:
   ```shell
   pip install diffoscope
   ```

> [!TIP]
> More info about `diffoscope` [here](https://diffoscope.org/)

#### Step 2: Reproduce the Build Locally

The local environment must exactly match the Continuous Integration (CI) environment used to create the release.

1. Clone the repository and checkout the specific tag to be verified:
   ```shell
   git clone https://github.com/LibreFitOrg/LibreFit.git
   cd LibreFit
   git checkout v1.0.0 # The version/tag you want to verify
   ```
2. Build the unsigned release APK using the exact same command as the CI workflow:
   ```shell
   ./gradlew clean assembleRelease --no-daemon
   ```
   The locally built unsigned APK will be generated at `app/build/outputs/apk/release/LibreFit-release-unsigned.apk`.

#### Step 3: Prepare the Release APK

1. Download the official signed APK (e.g., `LibreFit.apk`) from the [GitHub Releases](https://github.com/LibreFitOrg/LibreFit/releases) page
   or [F-Droid](https://f-droid.org/packages/org.librefit.app/).
2. Place the downloaded APK in the root of the project directory for easy access.

#### Step 4: Run the Comparison

With the virtual environment still activated, run `diffoscope` to compare the developer's signed APK against the locally built unsigned APK:

```shell
diffoscope LibreFit.apk app/build/outputs/apk/release/LibreFit-release-unsigned.apk
```

> [!NOTE]
> If `diffoscope` outputs warnings about `apksigner`, `androguard`, or `apktool` not being available, they can be safely ignored. `diffoscope` will
> automatically fall back to treating the APK as a standard ZIP archive, which is sufficient for verifying file contents.

#### Step 5: Analyze the Output

Upon running the command, `diffoscope` will output a structured diff of the two files.

**For a successfully reproducible build, the output must strictly match the following characteristics:**

1. **File Count Difference:** The output will indicate a difference in the total number of ZIP entries. The signed APK will have exactly **three more
   files** than the unsigned APK.
2. **Missing Files (`-` prefix):** The diff will show exactly three lines prefixed with a minus sign `-`, representing files present in the signed APK
   but absent from the unsigned APK. These will be located in the `META-INF/` directory:
   ```text
   --rw----     2.0 fat   279672 b- defN 81-Jan-01 01:01 META-INF/LIBREFIT.SF
   --rw----     2.0 fat     2178 b- defN 81-Jan-01 01:01 META-INF/LIBREFIT.RSA
   --rw----     2.0 fat   279545 b- defN 81-Jan-01 01:01 META-INF/MANIFEST.MF
   ```
3. **No Unexpected Changes (`+` prefix):** There should be **zero** lines prefixed with a plus sign `+` in the file listing. This proves the unsigned
   APK contains no extra or modified files.
4. **No Content Differences:** Beyond the `zipinfo` header changes (which reflect the 3 missing files and total byte size adjustments), there should
   be no recursive differences reported in files like `classes.dex`, `resources.arsc`, or `AndroidManifest.xml`.

If the output matches the criteria above, the verification is successful. The locally compiled source code perfectly matches the release, proving no
hidden code was injected during the CI/CD build or signing process.

#### Troubleshooting Failed Verifications

If `diffoscope` reports differences in compiled code or resources, the build is not reproducible. Common causes include:

* Mismatched JDK versions (the project requires JDK 21).
* Differences in Android Gradle Plugin (AGP) or Build Tools versions.

Consult the [F-Droid Reproducible Builds Documentation](https://f-droid.org/docs/Reproducible_Builds/) for detailed instructions on debugging
non-deterministic build issues.

> [!CAUTION]
> If issue persists, contact maintainers by either [filling form](https://librefit.org/contact)
> or [opening a new issue](https://github.com/LibreFitOrg/LibreFit/issues/new/choose)

#### Sample Log of Successful Verification

```shell
user@host:~/LibreFit$ python3 -m venv .venv
user@host:~/LibreFit$ source ./.venv/bin/activate
(.venv) user@host:~/LibreFit$ diffoscope LibreFit.apk LibreFit-release-unsigned.apk
--- LibreFit.apk
+++ LibreFit-release-unsigned.apk
│┄ 'apksigner' not available in path.
│┄ 'androguard' Python package not installed; cannot extract V2 signing keys.
│┄ 'apktool' not available in path. Format-specific differences are supported for Android APK files.
├── zipinfo {}
│ @@ -1,8 +1,8 @@
│ -Zip file size: 41190188 bytes, number of entries: 2693
│ +Zip file size: 40963505 bytes, number of entries: 2690
│  -rw-r--r--  0.0 unx       56 b- defN 81-Jan-01 01:01 META-INF/com/android/build/gradle/app-metadata.properties
│  -rw-r--r--  0.0 unx     7613 b- stor 81-Jan-01 01:01 assets/dexopt/baseline.prof
│  -rw-r--r--  0.0 unx     1178 b- stor 81-Jan-01 01:01 assets/dexopt/baseline.profm
│  -rw-r--r--  0.0 unx  9109820 b- defN 81-Jan-01 01:01 classes.dex
│  -rw-r--r--  0.0 unx  2386936 b- defN 81-Jan-01 01:01 classes2.dex
│  -rw-r--r--  0.0 unx    10096 b- stor 81-Jan-01 01:01 lib/arm64-v8a/libandroidx.graphics.path.so
│  -rw-r--r--  0.0 unx    10360 b- stor 81-Jan-01 01:01 lib/arm64-v8a/libdatastore_shared_counter.so
│ @@ -2685,11 +2685,8 @@
│  -rw----     0.0 fat      324 b- stor 81-Jan-01 01:01 res/zE.png
│  -rw----     0.0 fat      540 b- defN 81-Jan-01 01:01 res/zG.xml
│  -rw----     0.0 fat    18272 b- defN 81-Jan-01 01:01 res/zP.xml
│  -rw----     0.0 fat     2463 b- stor 81-Jan-01 01:01 res/zV.9.png
│  -rw----     0.0 fat      956 b- defN 81-Jan-01 01:01 res/zc.xml
│  -rw----     0.0 fat      464 b- defN 81-Jan-01 01:01 res/zq.xml
│  -rw----     0.0 fat   750708 b- stor 81-Jan-01 01:01 resources.arsc
│ --rw----     2.0 fat   279672 b- defN 81-Jan-01 01:01 META-INF/LIBREFIT.SF
│ --rw----     2.0 fat     2178 b- defN 81-Jan-01 01:01 META-INF/LIBREFIT.RSA
│ --rw----     2.0 fat   279545 b- defN 81-Jan-01 01:01 META-INF/MANIFEST.MF
│ -2693 files, 53039928 bytes uncompressed, 40746809 bytes compressed:  23.2%
│ +2690 files, 52478533 bytes uncompressed, 40529722 bytes compressed:  22.8%
```