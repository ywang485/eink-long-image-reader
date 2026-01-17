# Usage Guide

This guide will help you get started with the Eink Long Image Reader app.

## Getting Started

### First Launch

1. **Install the app** on your Android device (preferably an eink tablet)
2. **Launch the app** - you'll see a blank screen with a "Select Image" button at the bottom
3. **Grant permissions** when prompted to allow the app to access your storage

### Opening Your First Image

1. Tap the **"Select Image"** button at the bottom of the screen
2. Browse to a long image file (screenshot, article image, etc.)
3. Select the image
4. The app will automatically:
   - Load the image
   - Segment it into pages based on your screen size
   - Show the first page
   - Display "Page 1 / X" at the bottom

## Navigation

### Tap Navigation

The screen is divided into two invisible tap zones:

- **Left half of screen**: Tap to go to the **previous page**
- **Right half of screen**: Tap to go to the **next page**

This tap-based navigation is ideal for eink devices as it:
- Avoids scrolling lag
- Provides clean page transitions
- Minimizes screen refreshes

### Page Indicator

At the bottom of the screen, you'll see:
- Current page number
- Total number of pages
- Select Image button (to open a different image)

## Features in Detail

### Automatic Pagination

The app intelligently breaks your image into pages:

1. **Fits to screen width**: Images are scaled to match your device width
2. **Calculates page height**: Uses available screen height (minus status bar)
3. **Creates pages**: Segments the image into non-overlapping pages
4. **Optimizes for eink**: Each page is a static image with no scrolling

### Position Memory

The app remembers where you left off:

- **Per-image memory**: Each image has its own saved position
- **Automatic saving**: Position is saved when you:
  - Navigate to another page
  - Switch apps
  - Close the app
- **Resume reading**: Open the same image later to continue where you left off

### Persistent Image Access

Once you open an image:
- The app requests persistent permission to access it
- The image remains accessible even after restarting your device
- When you launch the app, it automatically loads your last image

## Tips for Eink Devices

### Optimize Your Experience

1. **Use high-contrast images**: Works best with black text on white backgrounds
2. **Avoid complex graphics**: Simpler images refresh better on eink screens
3. **Portrait orientation**: The app is optimized for portrait mode reading
4. **Adjust status bar height**: If pages don't align perfectly, see Configuration below

### Best Image Types

The app works great with:
- Long article screenshots
- Webpage captures
- Comic strips
- Document scans
- Vertical infographics

### Creating Long Images

To create article images for reading:

1. **Browser screenshots**: Use Firefox/Chrome screenshot tools to capture full pages
2. **Screenshot apps**: Use apps like LongShot or Stitch & Share
3. **Web to image**: Use services like web2img or screenshot.guru
4. **Document conversion**: Convert PDFs to images using online tools

## Common Use Cases

### Reading Web Articles

1. Take a full-page screenshot of an article in your browser
2. Share or save the screenshot
3. Open it in Eink Long Image Reader
4. Tap through pages at your own pace

### Reading Saved Twitter Threads

1. Use a thread unroller service to capture the thread as an image
2. Save the image to your device
3. Open in the app for easy reading

### Reading Comic Strips

1. Save vertical comic strips to your device
2. Open in the app
3. Navigate panel by panel using tap navigation

## Troubleshooting

### Image Doesn't Load

- **Check file format**: Ensure it's a standard image format (PNG, JPG, WebP)
- **Check file size**: Very large images (>20MB) may fail to load
- **Check permissions**: Ensure storage permissions are granted
- **Try reselecting**: Use the "Select Image" button to choose the file again

### Pages Are Misaligned

If page breaks don't match your screen perfectly:

1. The app estimates the status bar height
2. You can adjust this in the code (see Configuration in README.md)
3. Or use the app as-is with slight overlaps/gaps

### App Doesn't Remember Position

- **Use the file picker**: Sharing from other apps may not grant persistent access
- **Check permissions**: Ensure the app has storage permissions
- **Reselect the image**: Use "Select Image" to grant proper permissions

### Touch Areas Not Working

- Ensure you're tapping in the center of the left/right half of the screen
- Avoid tapping on the status bar at the bottom
- Try tapping more firmly

## Advanced Usage

### Multiple Images

To read multiple images:
1. Finish reading one image
2. Tap "Select Image"
3. Choose a new image
4. To return to the previous image, you'll need to reselect it

### Sharing Images to the App

The app registers as an image viewer, so you can:
1. In any app with an image (Gallery, Files, Browser)
2. Tap "Share" or "Open with"
3. Select "Eink Reader"
4. The image will open in the app

### Default Image Viewer

To set as default viewer:
1. Open an image in your file manager
2. Tap "Open with"
3. Select "Eink Reader"
4. Choose "Always" when prompted
5. Now all images will open in this app by default

## Keyboard Shortcuts (if using with keyboard)

While the app is primarily designed for touch:
- Volume Up/Down keys can be mapped to page navigation in some eink devices
- This requires device-specific configuration

## Performance Notes

### Memory Usage

- The app loads the entire image into memory
- Very large images (>50MB) may cause slowdown
- Pages are generated once and cached during the session
- Memory is freed when the app closes

### Best Practices

1. **Close other apps**: Free up memory for better performance
2. **Use compressed images**: JPEG instead of PNG for photos
3. **Optimize image size**: Images larger than 4000x10000px may be slow

## Privacy & Storage

### What the App Stores

- **Reading positions**: Stored locally in SharedPreferences
- **Last image URI**: Path to your most recent image
- **Persistent permissions**: Access to images you've opened

### What the App Does NOT Store

- No image copies are made
- No data is sent to the internet
- No analytics or tracking
- No account or login required

### Clearing Data

To reset the app:
1. Go to Settings > Apps > Eink Reader
2. Tap "Storage"
3. Tap "Clear Data"
4. This removes all saved positions and permissions

## Support

If you encounter issues:
1. Check this guide and the README troubleshooting section
2. Ensure your Android version is 5.0 or higher
3. Try reinstalling the app
4. Report bugs on the GitHub repository

## Enjoy Reading!

The app is designed to make reading long images on eink devices a pleasant experience. Tap through pages at your own pace without worrying about scrolling lag or screen refresh issues.

Happy reading!
