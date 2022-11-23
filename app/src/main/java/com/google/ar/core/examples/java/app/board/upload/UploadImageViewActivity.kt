package com.google.ar.core.examples.java.app.board.upload

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.ar.core.examples.java.app.board.DTO.BoardData
import com.google.ar.core.examples.java.app.board.DTO.UploadData
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.upload_main_recycler.*

/**
 * 업로드를 할때 사진을 고르는 activity
 * 사진은 local 갤러리가 아닌 firebase storage에 있는 자신의 사진에서 고른다
 * firebase storage에 저장된 사진은 Photozone Helper기능을 활용하여 촬영한 사진이다
 */

class UploadImageViewActivity : AppCompatActivity(){

    lateinit var uploadAdapter: UploadAdapter
    val datas = mutableListOf<BoardData>()
    val storageRef = FirebaseStorage.getInstance()

    val StringDownloadUrl = storageRef.reference.child("Gallery/userid/picID1.PNG").downloadUrl



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_main_recycler)
        var listManager = GridLayoutManager(this, 2)
        upload_main_recycler.layoutManager = listManager

        initRecycler()

        uploadAdapter.setOnItemClickListener(object : UploadAdapter.OnItemClickListener {
            override fun onItemClick(view: View, boardData: BoardData, position: Int) {
                Intent(this@UploadImageViewActivity, UploadActivity::class.java).apply {
                    putExtra("uploadData", boardData)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        })
    }
    private fun initRecycler() {
        uploadAdapter = UploadAdapter(this)
        upload_main_recycler.adapter = uploadAdapter



        datas.apply {
            add(
                    BoardData(img = "https://firebasestorage.googleapis.com/v0/b/toyproject-sns.appspot.com/o/post%2FD1TUcv401BUcKU8YVlMp4z41oJ73%2F1653846416681.jpg?alt=media&token=8f2295c2-2f55-4565-b9b4-f8afd1920300",
                    "테스트용 게시글입니다.",
                    2,
                    "2BXzuCaFIYXf7Dp06sHMCrTNSH43",
                    "Iron_Woong"))


            uploadAdapter.datas = datas
            uploadAdapter.notifyDataSetChanged()

        }

        val intent = Intent(this, UploadActivity::class.java)

    }
}