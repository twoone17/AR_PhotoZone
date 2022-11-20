package com.google.ar.core.examples.java.app.board

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.geospatial.R
import kotlinx.android.synthetic.main.activity_board_click.*

class BoardClickActivity : AppCompatActivity() {

    val TAG = "BoardClickActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_click)

        val intent = intent
        val boardData = intent.getSerializableExtra("boardData") as BoardData?

        if (boardData != null) {
            Glide.with(this).load(boardData.img).error(R.drawable.ic_baseline_error_outline_24).into(post_image)
            username.text = boardData.userId
            description.text = boardData.description
            likes.text = boardData.likes.toString()
        }

    }
}