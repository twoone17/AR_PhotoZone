package com.google.ar.core.examples.java.app.board

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.geospatial.R
import kotlinx.android.synthetic.main.activity_board_click.*

class BoardClickActivity : AppCompatActivity() {

    val TAG = "BoardClickActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_click)

        initBoardWithIntentData()
        pressLikeButton()
        pressSaveButton()
    }

    // 인텐트로 넘어온 데이터를 뷰에 뿌려주는 함수
    private fun initBoardWithIntentData() {
        val intent = intent
        val boardData = intent.getSerializableExtra("boardData") as BoardData?

        if (boardData != null) {
            Glide.with(this).load(boardData.img).error(R.drawable.ic_baseline_error_outline_24).into(post_image)
            username.text = boardData.userId
            description.text = boardData.description
            publisher.text = boardData.userId
            var likeString = boardData.likes.toString() + " likes"
            likes.text = likeString
            likes.text = likeString
        }
    }

    private fun pressLikeButton() {

    }

    private fun pressSaveButton() {

    }
}