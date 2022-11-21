package com.google.ar.core.examples.java.app.board.upload

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.examples.java.geospatial.R

class UploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val uploadButton = findViewById<TextView>(R.id.upload)
        val image_added = findViewById<ImageView>(R.id.image_added)
        uploadButton.setOnClickListener{
            println("LOG : 업로드 버튼 클릭")
            image_added.setOnClickListener{
                println("LOG : 사진 이미지 클릭 ")
                val nextIntent = Intent(this, UploadImageView::class.java)
                startActivity(nextIntent)
            }

        }

    }



    // 의성형 하고싶은거 다 해
}