package com.google.ar.core.examples.java.app.board.upload

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.examples.java.app.board.Boardfragment
import com.google.ar.core.examples.java.app.board.DTO.UploadData
import com.google.ar.core.examples.java.geospatial.R

class UploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val uploadButton = findViewById<TextView>(R.id.upload)
        val image_added = findViewById<ImageView>(R.id.image_added)
        val close = findViewById<ImageView>(R.id.close)

        val uploaddata = intent.getSerializableExtra("key") as UploadData?
        println("uploaddata = ${uploaddata}")

        uploadButton.setOnClickListener {
            println("!! 업로드 버튼 클릭 !!")
        }

        image_added.setOnClickListener {
            println("!! 사진 이미지 클릭 !!")
            val nextIntent = Intent(this, UploadImageViewActivity::class.java)
            startActivity(nextIntent)
        }

        close.setOnClickListener {
            println("!! close 클릭 !!")
            finish()
        }



    }


    // 의성형 하고싶은거 다 해
}