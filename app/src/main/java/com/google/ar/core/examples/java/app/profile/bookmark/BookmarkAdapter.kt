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

class BookmarkAdapter (val context : Context, val boardDataList : MutableList<BoardData>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        /* LayoutInflater는 item을 Adapter에서 사용할 View로 부풀려주는(inflate) 역할을 한다. */
        val view: View = LayoutInflater.from(context).inflate(R.layout.activity_bookmark_list_element, null)

        /* 위에서 생성된 view를 res-layout-main_lv_item.xml 파일의 각 View와 연결하는 과정이다. */
        val postImg = view.findViewById<ImageView>(R.id.postimg)
        val description = view.findViewById<TextView>(R.id.description)
        val name = view.findViewById<TextView>(R.id.name)

        /* ArrayList<Dog>의 변수 dog의 이미지와 데이터를 ImageView와 TextView에 담는다. */
        val postInfo = boardDataList[position]
        Glide.with(view).load(postInfo.imgURL).error(R.drawable.ic_baseline_error_outline_24).into(postImg)
        description.text = postInfo.description
        name.text = postInfo.userId

        return view
    }

    override fun getItem(position: Int): Any {
        return boardDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getCount(): Int {
        return boardDataList.size
    }
}