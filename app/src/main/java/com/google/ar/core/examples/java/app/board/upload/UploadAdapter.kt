package com.google.ar.core.examples.java.app.board.upload

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.geospatial.R

class UploadAdapter(private val context: Context) : RecyclerView.Adapter<UploadAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(view: View, boardData: BoardData, position : Int)
    }

    private var listener : OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    var datas = mutableListOf<BoardData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.upload_item_recycler,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])


    }



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imgProfile: ImageView = itemView.findViewById(R.id.upload_item_recycler)

        fun bind(item: BoardData) {
            Glide.with(itemView).load(item.img).error(R.drawable.ic_baseline_error_outline_24).into(imgProfile)

            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION) {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView, item, pos)
                }
            }
        }
    }


}
