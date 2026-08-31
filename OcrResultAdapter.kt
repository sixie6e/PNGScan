package com.example.pngscan

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class OcrResultItem(val uri: Uri, val fileName: String)

class OcrResultAdapter(
    private val onItemClick: (Uri) -> Unit
) : RecyclerView.Adapter<OcrResultAdapter.ViewHolder>() {

    private val items = mutableListOf<OcrResultItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.image_thumbnail)
        val fileName: TextView = view.findViewById(R.id.text_file_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ocr_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.fileName.text = item.fileName
        holder.thumbnail.setImageURI(item.uri)
        
        holder.itemView.setOnClickListener {
            onItemClick(item.uri)
        }
    }

    override fun getItemCount(): Int = items.size

    fun addResult(item: OcrResultItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun clear() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }
}
