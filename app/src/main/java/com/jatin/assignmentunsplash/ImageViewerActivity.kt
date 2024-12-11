package com.jatin.assignmentunsplash

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageViewerActivity : AppCompatActivity() {
    private lateinit var imgViewer: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_viewer)

        // Set edge-to-edge window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imgViewer = findViewById(R.id.imgViewer)

        val imageUrl = intent.getStringExtra("imageUrl")

        imageUrl?.let {
            GlobalScope.launch(Dispatchers.IO) {
                val bitmap = downloadBitmap(imageUrl)
                bitmap?.let { bmp ->
                    // Save image to local storage
                    val savedFilePath = saveImageToLocalStorage(bmp, "downloaded_image.jpg")

                    // Update UI on the main thread
                    launch(Dispatchers.Main) {
                        imgViewer.setImageBitmap(bmp)
                    }
                }
            }
        }
    }

    private fun downloadBitmap(imageUrl: String): Bitmap? {
        var bitmap: Bitmap? = null
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(imageUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
            inputStream?.close()
        }
        return bitmap
    }

    // Function to save the bitmap to local storage
    private fun saveImageToLocalStorage(bitmap: Bitmap, filename: String): String {
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES) // Save in app-specific directory
        val file = File(directory, filename)
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Save the bitmap as JPEG
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }

        return file.absolutePath // Return the saved file path
    }
}
