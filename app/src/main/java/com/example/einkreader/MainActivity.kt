package com.example.einkreader

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.ImageView
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

    private lateinit var prefs: SharedPreferences

    private var originalBitmap: Bitmap? = null
    private var pageBitmaps = mutableListOf<Bitmap>()
    private var currentPage = 0
    private var totalPages = 0

    private var currentImageUri: String? = null
    private var screenHeight = 0
    private var screenWidth = 0

    companion object {
        private const val PREFS_NAME = "EinkReaderPrefs"
        private const val KEY_LAST_PAGE_PREFIX = "last_page_"
        private const val KEY_LAST_IMAGE_URI = "last_image_uri"
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

        selectImageButton.setOnClickListener {
            openImageSelector()
        }
    }

    private fun initPreferences() {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

            // Available height for content (excluding status bar)
            val statusBarHeight = 100 // Approximate height of status bar in pixels
            val availableHeight = screenHeight - statusBarHeight

            // Calculate number of pages needed
            totalPages = Math.ceil(scaledHeight.toDouble() / availableHeight.toDouble()).toInt()

            // Create bitmap for each page
            for (pageIndex in 0 until totalPages) {
                val startY = (pageIndex * availableHeight / scaleFactor).toInt()
                val pageHeight = Math.min(
                    (availableHeight / scaleFactor).toInt(),
                    imageHeight - startY
                )

                if (pageHeight > 0) {
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
