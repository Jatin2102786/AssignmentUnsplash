package com.jatin.assignmentunsplash

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class ImageAdapter(private val context: MainActivity, private val images: MutableList<String>,private val listener: ImageClickListener) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.item_image)


    }

    interface ImageClickListener {
        abstract fun onImageClicked(imageUrl: String)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = images[position]
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            launch(Dispatchers.Main) {
                holder.imageView.setImageBitmap(bitmap)
            }
        }

        holder.itemView.setOnClickListener {
            listener.onImageClicked(imageUrl)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    fun addItems(newItems: List<String>) {
        val startPos = itemCount
        images.addAll(newItems)
        notifyItemRangeInserted(startPos, newItems.size)
    }
}
