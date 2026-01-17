# Building and Releasing the App

This document explains how to build the APK locally and create releases on GitHub.

## Prerequisites

- Android Studio (latest version recommended) OR
- JDK 17+ and Android SDK command line tools
- Git

## Building Locally

### Option 1: Using Android Studio

1. **Open the project**:
   ```bash
   git clone https://github.com/yourusername/eink-long-image-reader.git
   cd eink-long-image-reader
   ```

2. **Open in Android Studio**:
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Click OK

3. **Build the APK**:
   - For debug build: `Build > Build Bundle(s) / APK(s) > Build APK(s)`
   - For release build: `Build > Build Bundle(s) / APK(s) > Build APK(s)` then sign
   - APKs will be in `app/build/outputs/apk/`

### Option 2: Using Command Line

1. **Ensure you have the Gradle wrapper**:
   ```bash
   # If gradle/wrapper/gradle-wrapper.jar is missing, generate it:
   gradle wrapper --gradle-version 8.1.1
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

3. **Build Release APK** (unsigned):
   ```bash
   ./gradlew assembleRelease
   ```
   Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

4. **Build Release APK** (signed):
   - Create a keystore (one-time setup):
     ```bash
     keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
     ```

   - Add signing config to `app/build.gradle`:
     ```gradle
     android {
         ...
         signingConfigs {
             release {
                 storeFile file("../release-key.jks")
                 storePassword "your-store-password"
                 keyAlias "my-alias"
                 keyPassword "your-key-password"
             }
         }
         buildTypes {
             release {
                 signingConfig signingConfigs.release
                 ...
             }
         }
     }
     ```

   - Build:
     ```bash
     ./gradlew assembleRelease
     ```

## Automated Builds with GitHub Actions

The repository includes a GitHub Actions workflow that automatically builds APKs.

### Triggering Builds

**Automatic builds trigger on**:
- Push to `main` branch
- Pull requests to `main`
- Manual workflow dispatch

**Release creation triggers on**:
- Pushing a version tag (e.g., `v1.0.0`)

### Creating a Release

1. **Tag your commit**:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

2. **GitHub Actions will**:
   - Build both debug and release APKs
   - Create a new GitHub Release
   - Attach the APKs to the release
   - Generate release notes automatically

3. **View the release**:
   - Go to your repository on GitHub
   - Click "Releases" in the sidebar
   - Your new release will appear with downloadable APKs

### Manual Workflow Trigger

You can also trigger builds manually:

1. Go to your repository on GitHub
2. Click "Actions" tab
3. Select "Build and Release APK" workflow
4. Click "Run workflow"
5. Select the branch and click "Run workflow"

The built APKs will be available as artifacts (downloadable for 90 days).

## Installing the APK

### On Your Device

1. **Enable installation from unknown sources**:
   - Go to Settings > Security
   - Enable "Unknown Sources" or "Install from Unknown Sources"

2. **Install the APK**:
   - Transfer the APK to your device
   - Tap the APK file
   - Follow the installation prompts

### Using ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or for release:
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

## Troubleshooting

### Gradle Wrapper Missing

If you get errors about missing gradle-wrapper.jar:

```bash
gradle wrapper --gradle-version 8.1.1
```

Then commit the generated files:
```bash
git add gradle/wrapper/gradle-wrapper.jar
git commit -m "Add Gradle wrapper jar"
git push
```

### Build Fails in GitHub Actions

- Check the Actions tab for detailed logs
- Ensure all required files are committed
- Verify the workflow file syntax

### SDK Not Found

If building locally and SDK is not found:

1. **Set ANDROID_HOME**:
   ```bash
   export ANDROID_HOME=$HOME/Android/Sdk
   ```

2. **Or install Android SDK**:
   - Download Android Studio
   - Or use sdkmanager command line tools

## Release Checklist

Before creating a new release:

- [ ] Update version code in `app/build.gradle`
- [ ] Update version name in `app/build.gradle`
- [ ] Update CHANGELOG.md with new features/fixes
- [ ] Test the app thoroughly
- [ ] Commit all changes
- [ ] Create and push version tag
- [ ] Verify GitHub Actions build succeeds
- [ ] Test the released APK on real device
- [ ] Update README if needed

## Version Numbering

Follow semantic versioning:
- **Major** (v1.0.0): Breaking changes
- **Minor** (v1.1.0): New features, backwards compatible
- **Patch** (v1.1.1): Bug fixes

Update both `versionCode` (integer) and `versionName` (string) in `app/build.gradle`:

```gradle
android {
    defaultConfig {
        versionCode 2        // Increment by 1 for each release
        versionName "1.0.1"  // Semantic version string
    }
}
```

## Continuous Integration

The GitHub Actions workflow provides:

- **Automated builds** on every push
- **APK artifacts** for testing (available for 90 days)
- **Automated releases** when tags are pushed
- **Build caching** for faster builds

You can customize the workflow in `.github/workflows/build-apk.yml`.
