package com.google.ar.core.examples.java.app.profile.bookmark

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_board_click.*

class BookmarkAdapter (val context : Context) : BaseAdapter() {

    val boardDataList = mutableListOf<BoardData>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View = LayoutInflater.from(context).inflate(R.layout.activity_bookmark_list_element, null)

        val postImg = view.findViewById<ImageView>(R.id.postimg)
        val description = view.findViewById<TextView>(R.id.description)
        val name = view.findViewById<TextView>(R.id.name)

        val db = Firebase.firestore

        val postInfo = boardDataList[position]
        Glide.with(view).load(postInfo.imgURL).error(R.drawable.ic_baseline_error_outline_24).into(postImg)
        description.text = postInfo.description
        db.collection("users").document(postInfo.userId).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val document = task.result
                name.text = document.get("userName").toString()
            }
        }
        return view
    }

    fun addItem(data: BoardData) {
        boardDataList.add(data)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): BoardData {
        return boardDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getCount(): Int {
        return boardDataList.size
    }
}