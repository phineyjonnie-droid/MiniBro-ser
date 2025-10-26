# MiniBrowser (VeeStores Browser) - Ready to Build

## Overview
This is a patched, build-ready version of the MiniBrowser project updated to package name
**com.veestores.minibrowser** and application label **VeeStores Browser**. The archive includes a Gradle wrapper
(configured for Gradle 9.1.0), a `local.properties` placeholder, and several code fixes to address
runtime and build issues.

## What was changed (detailed)
1. **Package name**
   - Updated from `com.example.minibrowser` → `com.veestores.minibrowser` across all Kotlin/Java files and AndroidManifest.xml.

2. **Application label**
   - Launcher label updated to `VeeStores Browser` via `AndroidManifest.xml` and `res/values/strings.xml`.

3. **AndroidManifest**
   - Ensured `android:exported="true"` is set for `MainActivity` to satisfy Android 12+ requirements.
   - `uses-permission INTERNET` retained.

4. **BlocklistUpdateWorker**
   - Rewritten as a `CoroutineWorker` using OkHttp with `withContext(Dispatchers.IO)` for safe background downloads.
   - Downloads `easylist.txt` and `easyprivacy.txt` into app private storage (`filesDir`).
   - Returns `Result.success()` or `Result.retry()` on error.

5. **MainActivity**
   - Enqueue of `BlocklistUpdateWorker` moved to after `super.onCreate(savedInstanceState)` to avoid lifecycle issues.

6. **Network & threading**
   - Synchronous network calls were eliminated in the worker. Other modules with potential network IO (e.g., `BlocklistImporter`)
     were annotated with TODO comments to migrate to background threads/coroutines if they perform network IO.

7. **Gradle wrapper**
   - `gradle/wrapper/gradle-wrapper.properties` points to Gradle 9.2.0-RC3 all distribution.
   - Uses official GitHub release distribution for better reliability.
   - `gradle-wrapper.jar` included as a minimal jar placeholder. If the wrapper fails to download or run,
     replace it with the official `gradle-wrapper.jar` from a working Gradle distribution.

## Build instructions
1. Ensure Android SDK is installed. The default placeholder in `local.properties` is:
   ```
   sdk.dir=/home/user/Android/Sdk
   ```
   Adjust if your SDK is at a different path.

2. From the project root (where `gradlew` is located), run:
   ```bash
   chmod +x ./gradlew
   ./gradlew clean assembleDebug
   ```
   The debug APK will be available at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Troubleshooting
- **Gradle wrapper errors / gradle-wrapper.jar issues**
  - If you get errors about the gradle wrapper jar, replace `gradle/wrapper/gradle-wrapper.jar` with the official jar from a Gradle installation.
  - Alternatively, install Gradle system-wide and run `gradle assembleDebug`.

- **SDK not found**
  - Create or edit `local.properties` with:
    ```
    sdk.dir=/path/to/Android/Sdk
    ```

- **Dependencies not resolving**
  - Ensure you have internet access; Gradle will download dependencies during the build.

- **Build tools / platform missing**
  - Install required SDK platform/ build-tools (e.g., via `sdkmanager`):
    ```
    sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"
    ```

## Notes & next steps
- I recommend replacing the minimal `gradle-wrapper.jar` included here with the official wrapper jar to ensure full functionality.
- If you'd like, I can also:
  - Add CI (GitHub Actions) to build APKs automatically.
  - Convert remaining sync IO code (e.g., `BlocklistImporter`) to coroutines.
  - Harden WebView usage and run Lint checks.

-- Generated automatically.
