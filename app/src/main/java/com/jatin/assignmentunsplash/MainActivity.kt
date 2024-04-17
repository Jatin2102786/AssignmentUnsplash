package com.jatin.assignmentunsplash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(),ImageAdapter.ImageClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var currentPage = 1
    private var perPage = 10
    private val imageUrls = mutableListOf<String>()
    private var apiRequestCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 3600000

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this,2)
        adapter = ImageAdapter(this, imageUrls,this)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    currentPage++
                    makeApiRequest()
                }
            }
        })

        makeApiRequest()
        scheduleHourlyRefresh()



    }

    private var isLoading = false

    private fun fetchImages() {
        isLoading = true
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("https://api.unsplash.com/photos/random?count=$perPage&page=$currentPage&client_id=Unsplash_Api_Access_key")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val inputStream = BufferedInputStream(connection.inputStream)
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonArray = JSONArray(response)
            val newImageUrls = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val imageUrl = jsonObject.getJSONObject("urls").getString("regular")
                newImageUrls.add(imageUrl)
            }

            launch(Dispatchers.Main) {
                adapter.addItems(newImageUrls)
            }

            isLoading = false
        }

    }

    private fun makeApiRequest() {
        apiRequestCount++

        if (apiRequestCount > 50) {
            showApiRequestLimitExceededMessage()
            return
        }

        try {
            fetchImages()
            Thread.sleep(1000)
            showApiRequestSuccessMessage()
        } catch (e: Exception) {
            showApiRequestFailedMessage()
        }
    }

    private fun showApiRequestLimitExceededMessage() {
        runOnUiThread {
            Toast.makeText(this, "API request limit exceeded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showApiRequestSuccessMessage() {

    }

    private fun showApiRequestFailedMessage() {
        runOnUiThread {
            Toast.makeText(this, "Failed to make API request", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleHourlyRefresh() {
        handler.postDelayed({
            resetApiRequestCount()
            scheduleHourlyRefresh()
        }, refreshInterval.toLong())
    }

    private fun resetApiRequestCount() {
        apiRequestCount = 0
    }


    override fun onImageClicked(imageUrl: String) {
        val intent = Intent(this, ImageViewerActivity::class.java)
        intent.putExtra("imageUrl", imageUrl)
        startActivity(intent)
        print(imageUrl)
    }
}

