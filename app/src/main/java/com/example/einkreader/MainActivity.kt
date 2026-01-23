package com.example.einkreader

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var pageIndicator: TextView
    private lateinit var selectImageButton: Button
    private lateinit var leftTouchArea: View
    private lateinit var rightTouchArea: View
    private lateinit var goToPageButton: Button
    private lateinit var settingsButton: Button
    private lateinit var controlsPanel: View

    // Image controls
    private lateinit var overlapSeekBar: SeekBar
    private lateinit var overlapValue: TextView
    private lateinit var brightnessSeekBar: SeekBar
    private lateinit var brightnessValue: TextView
    private lateinit var contrastSeekBar: SeekBar
    private lateinit var contrastValue: TextView
    private lateinit var invertColorButton: Button
    private lateinit var resetImageButton: Button

    private lateinit var prefs: SharedPreferences

    private var originalBitmap: Bitmap? = null
    private var pageBitmaps = mutableListOf<Bitmap>()
    private var currentPage = 0
    private var totalPages = 0

    private var currentImageUri: String? = null
    private var screenHeight = 0
    private var screenWidth = 0

    // Image adjustment settings
    private var pageOverlapPercent = 0
    private var brightness = 100f
    private var contrast = 100f
    private var isColorInverted = false

    companion object {
        private const val PREFS_NAME = "EinkReaderPrefs"
        private const val KEY_LAST_PAGE_PREFIX = "last_page_"
        private const val KEY_LAST_IMAGE_URI = "last_image_uri"
        private const val KEY_PAGE_OVERLAP = "page_overlap"
        private const val KEY_BRIGHTNESS = "brightness"
        private const val KEY_CONTRAST = "contrast"
        private const val KEY_INVERT_COLOR = "invert_color"
        private const val REQUEST_CODE_SELECT_IMAGE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initPreferences()
        getScreenDimensions()
        setupTouchListeners()

        // Handle intent if app was opened with an image
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        pageIndicator = findViewById(R.id.pageIndicator)
        selectImageButton = findViewById(R.id.selectImageButton)
        leftTouchArea = findViewById(R.id.leftTouchArea)
        rightTouchArea = findViewById(R.id.rightTouchArea)
        goToPageButton = findViewById(R.id.goToPageButton)
        settingsButton = findViewById(R.id.settingsButton)
        controlsPanel = findViewById(R.id.controlsPanel)

        // Image controls
        overlapSeekBar = findViewById(R.id.overlapSeekBar)
        overlapValue = findViewById(R.id.overlapValue)
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar)
        brightnessValue = findViewById(R.id.brightnessValue)
        contrastSeekBar = findViewById(R.id.contrastSeekBar)
        contrastValue = findViewById(R.id.contrastValue)
        invertColorButton = findViewById(R.id.invertColorButton)
        resetImageButton = findViewById(R.id.resetImageButton)

        selectImageButton.setOnClickListener {
            openImageSelector()
        }

        goToPageButton.setOnClickListener {
            showGoToPageDialog()
        }

        settingsButton.setOnClickListener {
            toggleControlsPanel()
        }

        setupImageControls()
    }

    private fun setupImageControls() {
        // Page overlap control
        overlapSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pageOverlapPercent = progress
                overlapValue.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.edit().putInt(KEY_PAGE_OVERLAP, pageOverlapPercent).apply()
                if (originalBitmap != null) {
                    segmentImageIntoPages()
                    currentPage = 0
                    displayCurrentPage()
                    Toast.makeText(this@MainActivity, "Pages regenerated with $pageOverlapPercent% overlap", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Brightness control
        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightness = progress.toFloat()
                brightnessValue.text = "$progress"
                applyImageFilters()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.edit().putFloat(KEY_BRIGHTNESS, brightness).apply()
            }
        })

        // Contrast control
        contrastSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                contrast = progress.toFloat()
                contrastValue.text = "$progress"
                applyImageFilters()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.edit().putFloat(KEY_CONTRAST, contrast).apply()
            }
        })

        // Color invert button
        invertColorButton.setOnClickListener {
            isColorInverted = !isColorInverted
            invertColorButton.text = if (isColorInverted) "Invert Colors: ON" else "Invert Colors: OFF"
            prefs.edit().putBoolean(KEY_INVERT_COLOR, isColorInverted).apply()
            applyImageFilters()
        }

        // Reset image adjustments button
        resetImageButton.setOnClickListener {
            brightness = 100f
            contrast = 100f
            isColorInverted = false
            brightnessSeekBar.progress = 100
            contrastSeekBar.progress = 100
            invertColorButton.text = "Invert Colors: OFF"
            prefs.edit()
                .putFloat(KEY_BRIGHTNESS, brightness)
                .putFloat(KEY_CONTRAST, contrast)
                .putBoolean(KEY_INVERT_COLOR, isColorInverted)
                .apply()
            applyImageFilters()
            Toast.makeText(this, "Image adjustments reset", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleControlsPanel() {
        if (controlsPanel.visibility == View.GONE) {
            controlsPanel.visibility = View.VISIBLE
            settingsButton.text = "Hide Settings"
        } else {
            controlsPanel.visibility = View.GONE
            settingsButton.text = "Settings"
        }
    }

    private fun showGoToPageDialog() {
        if (totalPages == 0) {
            Toast.makeText(this, "Please load an image first", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this)
        input.hint = "Enter page number (1-$totalPages)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Go to Page")
            .setMessage("Enter page number:")
            .setView(input)
            .setPositiveButton("Go") { _, _ ->
                val pageStr = input.text.toString()
                try {
                    val page = pageStr.toInt()
                    if (page in 1..totalPages) {
                        currentPage = page - 1
                        displayCurrentPage()
                    } else {
                        Toast.makeText(this, "Page must be between 1 and $totalPages", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyImageFilters() {
        val colorMatrix = ColorMatrix()

        // Apply brightness and contrast
        val brightnessValue = (brightness - 100) / 100f * 255f
        val contrastValue = contrast / 100f

        val scale = contrastValue
        val translate = brightnessValue

        colorMatrix.set(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))

        // Apply color inversion if enabled
        if (isColorInverted) {
            val invertMatrix = ColorMatrix(floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            ))
            colorMatrix.postConcat(invertMatrix)
        }

        imageView.colorFilter = ColorMatrixColorFilter(colorMatrix)
    }

    private fun initPreferences() {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load saved settings
        pageOverlapPercent = prefs.getInt(KEY_PAGE_OVERLAP, 0)
        brightness = prefs.getFloat(KEY_BRIGHTNESS, 100f)
        contrast = prefs.getFloat(KEY_CONTRAST, 100f)
        isColorInverted = prefs.getBoolean(KEY_INVERT_COLOR, false)

        // Update UI with saved values
        overlapSeekBar.progress = pageOverlapPercent
        overlapValue.text = "$pageOverlapPercent%"
        brightnessSeekBar.progress = brightness.toInt()
        brightnessValue.text = brightness.toInt().toString()
        contrastSeekBar.progress = contrast.toInt()
        contrastValue.text = contrast.toInt().toString()
        invertColorButton.text = if (isColorInverted) "Invert Colors: ON" else "Invert Colors: OFF"

        // Try to load last opened image
        val lastImageUri = prefs.getString(KEY_LAST_IMAGE_URI, null)
        if (lastImageUri != null) {
            currentImageUri = lastImageUri
            loadImageFromUri(Uri.parse(lastImageUri))
        }
    }

    private fun getScreenDimensions() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels
    }

    private fun setupTouchListeners() {
        leftTouchArea.setOnClickListener {
            previousPage()
        }

        rightTouchArea.setOnClickListener {
            nextPage()
        }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    loadImageFromUri(uri)
                }
            }
            Intent.ACTION_MAIN -> {
                // Already handled in initPreferences
            }
        }
    }

    private fun openImageSelector() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Take persistable permission for the URI
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Permission not available for this URI
                }
                loadImageFromUri(uri)
            }
        }
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                currentImageUri = uri.toString()
                saveLastImageUri(uri.toString())

                segmentImageIntoPages()

                // Load saved page position or start from beginning
                val savedPage = getSavedPagePosition(uri.toString())
                currentPage = if (savedPage < totalPages) savedPage else 0

                displayCurrentPage()
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun segmentImageIntoPages() {
        originalBitmap?.let { bitmap ->
            pageBitmaps.clear()

            val imageWidth = bitmap.width
            val imageHeight = bitmap.height

            // Calculate the scaled dimensions to fit screen width
            val scaleFactor = screenWidth.toFloat() / imageWidth.toFloat()
            val scaledHeight = (imageHeight * scaleFactor).toInt()

            // Available height for content (excluding status bar and controls)
            val statusBarHeight = 100 // Approximate height of status bar in pixels
            val availableHeight = screenHeight - statusBarHeight

            // Calculate overlap in pixels (in original image coordinates)
            val overlapPixels = (availableHeight * pageOverlapPercent / 100.0 / scaleFactor).toInt()

            // Effective page height considering overlap
            val effectivePageHeight = (availableHeight / scaleFactor).toInt() - overlapPixels

            // Calculate number of pages needed
            val pagesNeeded = if (effectivePageHeight > 0) {
                Math.ceil((imageHeight - overlapPixels).toDouble() / effectivePageHeight.toDouble()).toInt()
            } else {
                Math.ceil(scaledHeight.toDouble() / availableHeight.toDouble()).toInt()
            }

            // Create bitmap for each page
            for (pageIndex in 0 until pagesNeeded) {
                val startY = if (pageOverlapPercent > 0 && effectivePageHeight > 0) {
                    pageIndex * effectivePageHeight
                } else {
                    (pageIndex * availableHeight / scaleFactor).toInt()
                }

                val pageHeight = Math.min(
                    (availableHeight / scaleFactor).toInt(),
                    imageHeight - startY
                )

                if (pageHeight > 0 && startY < imageHeight) {
                    try {
                        val pageBitmap = Bitmap.createBitmap(
                            bitmap,
                            0,
                            startY,
                            imageWidth,
                            pageHeight
                        )
                        pageBitmaps.add(pageBitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            totalPages = pageBitmaps.size
        }
    }

    private fun displayCurrentPage() {
        if (currentPage < pageBitmaps.size) {
            imageView.setImageBitmap(pageBitmaps[currentPage])
            applyImageFilters()
            updatePageIndicator()
            saveCurrentPagePosition()
        }
    }

    private fun updatePageIndicator() {
        pageIndicator.text = "Page ${currentPage + 1} / $totalPages"
    }

    private fun nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++
            displayCurrentPage()
        } else {
            Toast.makeText(this, "Last page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun previousPage() {
        if (currentPage > 0) {
            currentPage--
            displayCurrentPage()
        } else {
            Toast.makeText(this, "First page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCurrentPagePosition() {
        currentImageUri?.let { uri ->
            prefs.edit()
                .putInt(KEY_LAST_PAGE_PREFIX + uri.hashCode(), currentPage)
                .apply()
        }
    }

    private fun getSavedPagePosition(uri: String): Int {
        return prefs.getInt(KEY_LAST_PAGE_PREFIX + uri.hashCode(), 0)
    }

    private fun saveLastImageUri(uri: String) {
        prefs.edit()
            .putString(KEY_LAST_IMAGE_URI, uri)
            .apply()
    }

    override fun onPause() {
        super.onPause()
        // Save position when app goes to background
        saveCurrentPagePosition()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up bitmaps
        pageBitmaps.forEach { it.recycle() }
        pageBitmaps.clear()
        originalBitmap?.recycle()
    }
}
