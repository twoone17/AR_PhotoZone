package com.google.ar.core.examples.java.app.board

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.core.examples.java.geospatial.R

class BoardClickActivity : AppCompatActivity() {

    val TAG = "BoardClickActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_click)

        val intent = intent
        val boardData = intent.getSerializableExtra("boardData") as BoardData?
        if (boardData != null) {
            Log.e(TAG, "onCreate: " + boardData.description)
        }
    }
}