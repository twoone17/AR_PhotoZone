package com.google.ar.core.examples.java.app.board
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.geospatial.R

class BoardAdapter(private val context: Context) : RecyclerView.Adapter<BoardAdapter.ViewHolder>() {

    var datas = mutableListOf<BoardData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_board_recycler_element,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imgProfile: ImageView = itemView.findViewById(R.id.img_rv_photo)

        fun bind(item: BoardData) {
            Glide.with(itemView).load(item.img).error(R.drawable.ic_baseline_error_outline_24).into(imgProfile)
        }
    }


}