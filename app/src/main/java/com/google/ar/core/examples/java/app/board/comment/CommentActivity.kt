package com.google.ar.core.examples.java.app.board.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.geospatial.R

class CommentActivity : AppCompatActivity() {

    val TAG = "CommentActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val intent = intent
        val documentId = intent.getStringExtra("post_document_Id")
        Log.e(TAG, "onCreate: " + documentId )
    }
}