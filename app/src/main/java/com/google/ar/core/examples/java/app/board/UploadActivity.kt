package com.google.ar.core.examples.java.app.board

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.app.board.upload.UploadImageViewActivity
import com.google.ar.core.examples.java.geospatial.R
import kotlinx.android.synthetic.main.activity_board_click.*

class UploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val uploadButton = findViewById<TextView>(R.id.upload)
        val image_added = findViewById<ImageView>(R.id.image_added)
        val close = findViewById<ImageView>(R.id.close)

        val uploaddata = intent.getSerializableExtra("uploadData") as BoardData?
        println("uploaddata = ${uploaddata}")

        if(uploaddata!=null) {
            Glide.with(this).load(uploaddata!!.imgURL).error(R.drawable.ic_baseline_error_outline_24).into(image_added)
        }
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