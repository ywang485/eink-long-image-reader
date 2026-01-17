# Initial Setup

## Important: Gradle Wrapper Setup

Before you can build the app locally or have GitHub Actions build it, you need to generate the Gradle wrapper jar file.

### Option 1: Generate Locally (Recommended)

If you have Gradle installed on your machine:

```bash
gradle wrapper --gradle-version 8.1.1
```

This will generate `gradle/wrapper/gradle-wrapper.jar`.

Then commit and push it:

```bash
git add gradle/wrapper/gradle-wrapper.jar
git commit -m "Add Gradle wrapper jar"
git push
```

### Option 2: Use Android Studio

1. Open the project in Android Studio
2. Android Studio will automatically generate the wrapper
3. Commit the generated `gradle/wrapper/gradle-wrapper.jar`

### Option 3: Download Manually

If you don't have Gradle installed:

1. Download the Gradle wrapper jar from a trusted source or another Android project
2. Place it in `gradle/wrapper/gradle-wrapper.jar`
3. Commit and push it

## After Wrapper Setup

Once the wrapper jar is committed:

1. **Local builds** will work:
   ```bash
   ./gradlew assembleDebug
   ```

2. **GitHub Actions** will automatically build APKs on push

3. **Creating releases** with tags will automatically publish APKs:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

## Quick Start for Development

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/eink-long-image-reader.git
   cd eink-long-image-reader
   ```

2. **Generate Gradle wrapper** (if not already present):
   ```bash
   gradle wrapper --gradle-version 8.1.1
   git add gradle/wrapper/gradle-wrapper.jar
   git commit -m "Add Gradle wrapper jar"
   git push
   ```

3. **Open in Android Studio** and let it sync

4. **Run on device**: Click the Run button or use:
   ```bash
   ./gradlew installDebug
   ```

## Creating Your First Release

1. **Ensure wrapper is set up** (see above)

2. **Build locally to test**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Tag the release**:
   ```bash
   git tag -a v1.0.0 -m "First release"
   git push origin v1.0.0
   ```

4. **Check GitHub Actions**:
   - Go to the Actions tab on GitHub
   - Watch the build complete
   - APKs will be automatically attached to the release

5. **Download and test** the APK from the Releases page

That's it! The app is now ready for development and automated releases.
