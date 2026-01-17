# Eink Long Image Reader

An Android app specifically designed for reading long article images on eink tablets with slow refresh rates. The app intelligently segments long images into screen-sized pages and provides tap-based navigation, eliminating the need for scrolling on eink devices.

## Features

- **Smart Pagination**: Automatically segments long images into pages based on your device's screen size
- **Tap Navigation**: Navigate between pages by tapping the left (previous) or right (next) side of the screen
- **Position Memory**: Automatically remembers which page you were on for each image
- **Eink Optimized**: Designed to minimize screen refreshes and avoid scrolling on eink displays
- **File Picker Integration**: Easily open any image from your device storage
- **Persistent Access**: Maintains access to previously opened images across app sessions

## Installation

### Option 1: Build from Source

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/eink-long-image-reader.git
   cd eink-long-image-reader
   ```

2. Open the project in Android Studio

3. Build and install:
   - Connect your Android device or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### Option 2: Install APK

Download the latest APK from the [Releases](../../releases) page and install it on your device.

## Usage

### Opening Images

1. **Launch the app** and tap the "Select Image" button
2. **Browse** to find your long article image
3. The app will automatically:
   - Segment the image into pages
   - Remember your reading position
   - Display page numbers at the bottom

### Navigation

- **Tap the right side** of the screen to go to the next page
- **Tap the left side** of the screen to go to the previous page
- **Page indicator** at the bottom shows current page and total pages

### Position Memory

- The app automatically saves your current page when:
  - You navigate to a different page
  - You switch to another app
  - You close the app
- When you reopen the same image, it will resume from where you left off

### Opening Images from Other Apps

The app registers as a handler for image files, so you can:
- Share images to the app from your browser or file manager
- Set it as the default viewer for long screenshots

## Requirements

- **Android 5.0 (API 21)** or higher
- Optimized for **eink tablets** but works on any Android device

## Technical Details

### How Pagination Works

1. The app loads the full image into memory
2. Calculates the screen dimensions (width and height)
3. Scales the image to fit the screen width
4. Divides the scaled height by available screen height to determine page count
5. Creates individual bitmap segments for each page
6. Displays one page at a time without scrolling

### Storage

- **SharedPreferences** stores:
  - Last opened image URI
  - Page position for each image (keyed by URI hash)
- **Persistable URI permissions** allow the app to access images across sessions
- No images are copied or duplicated - the app reads directly from your storage

### Eink Optimization

- Static page display (no scrolling animations)
- Minimal UI elements
- Simple tap gestures instead of swipe gestures
- White background with high contrast
- Memory-efficient bitmap handling

## Configuration

### Adjusting Status Bar Height

If the page segmentation doesn't perfectly match your screen, you can adjust the status bar height in `MainActivity.kt`:

```kotlin
val statusBarHeight = 100 // Change this value
```

### Screen Orientation

The app is configured for portrait mode by default. To change this, edit `AndroidManifest.xml`:

```xml
android:screenOrientation="portrait"  <!-- Change to "landscape" if needed -->
```

## Troubleshooting

### Images Not Loading

- Ensure you've granted storage permissions
- Try selecting the image again using the file picker
- Check that the image file is not corrupted

### Pages Don't Match Screen

- The app uses approximate calculations for the status bar
- Adjust `statusBarHeight` in MainActivity.kt for your device

### App Doesn't Remember Position

- Ensure the app has permission to access the file
- Use "Select Image" button instead of sharing from other apps for persistent access

## Development

### Project Structure

```
eink-long-image-reader/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/einkreader/
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml
│   │   │   └── values/
│   │   │       └── strings.xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── README.md
```

### Key Classes

- **MainActivity.kt**: Main activity handling image loading, pagination, and navigation
- **SharedPreferences**: For persisting reading positions

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing configuration)
./gradlew assembleRelease
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the [MIT License](LICENSE).

## Future Enhancements

Potential features to add:
- Bookmark specific pages
- Zoom functionality for detailed viewing
- Dark mode for regular displays
- Adjust page overlap for context
- Image brightness/contrast controls
- Multiple image library management
- Export reading progress

## Credits

Developed for eink tablet users who want a better experience reading long article screenshots and images without the lag of scrolling on eink displays.
