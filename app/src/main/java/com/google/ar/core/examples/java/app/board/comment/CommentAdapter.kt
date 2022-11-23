package com.google.ar.core.examples.java.app.board.comment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.app.board.BoardAdapter
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.geospatial.R

class CommentAdapter(private val context: Context) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(view: View, commentData: CommentData, position : Int)
    }

    private var listener : OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    var datas = mutableListOf<CommentData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_comment_recycler_element,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])


    }



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imgProfile: ImageView = itemView.findViewById(R.id.image_profile)

        fun bind(item: CommentData) {
            // Glide.with(itemView).load(item.imgURL).error(R.drawable.ic_baseline_error_outline_24).into(imgProfile)
            // 댓글에 들어가는 사진은 댓글 작성시에 삽입되는게 아니라 동적으로 서버에서 받아와야 한다.
            // 그렇지 않으면 프로필 사진을 바꿔도 먼저 작성해둔 댓글의 사진이 바뀌지 않는 문제가 발생할 것으로 추측된다.

             Glide.with(itemView).load("추후 프로필사진 받아올 예정").error(R.drawable.ic_baseline_error_outline_24).into(imgProfile)

            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION) {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView, item, pos)
                }
            }
        }
    }
}