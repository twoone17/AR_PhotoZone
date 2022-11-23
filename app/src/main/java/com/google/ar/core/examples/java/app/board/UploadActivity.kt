package com.google.ar.core.examples.java.app.board

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.app.board.upload.UploadImageViewActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class UploadActivity : AppCompatActivity() {
    private var auth = Firebase.auth
    private val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    var imgURL:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val uploadButton = findViewById<TextView>(R.id.upload)
        val image_added = findViewById<ImageView>(R.id.image_added)
        val close = findViewById<ImageView>(R.id.close)

        val uploaddata = intent.getSerializableExtra("uploadData") as BoardData?
        println("uploaddata = ${uploaddata}")


        //사진을 고르면 고른 사진을 띄워준다
        if (uploaddata != null) {
            imgURL = uploaddata!!.imgURL
            Glide.with(this).load(imgURL).error(R.drawable.ic_baseline_error_outline_24).into(image_added)
        }


        uploadButton.setOnClickListener {
            println("!! 업로드 버튼 클릭 !!")
            val now = LocalDateTime.now()
            val documentID = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH))
            val data = BoardData(imgURL = imgURL!!,
                    description = "업로드 연습용 description1",
                    likes = 5,
                    publisher = auth.currentUser!!.uid,
                    userId = auth.currentUser!!.uid,
                    documentID)

            db.collection("app_board").document(documentID).set(data!!)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "게시글 작성완료", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "게시글 작성 실패", Toast.LENGTH_LONG).show()
                    }
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