package com.google.ar.core.examples.java.app.board.upload

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.app.board.UploadActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.upload_main_recycler.*

/**
 * 업로드를 할때 사진을 고르는 activity
 * 사진은 local 갤러리가 아닌 firebase storage에 있는 자신의 사진에서 고른다
 * firebase storage에 저장된 사진은 Photozone Helper기능을 활용하여 촬영한 사진이다
 */

class UploadImageViewActivity : AppCompatActivity() {

    lateinit var uploadAdapter: UploadAdapter
    val datas = mutableListOf<BoardData>()
    val storageRef = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()
    val StringDownloadUrl = storageRef.reference.child("Gallery/userid/picID1.PNG").downloadUrl
    private var auth = Firebase.auth
    private val currentUser = auth.currentUser


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

        db.collection("users").document(auth.currentUser!!.uid).collection("posts").get()
            .addOnSuccessListener { result ->
                for (links in result) {
                    datas.apply {
                        add(
                            BoardData(
                                imgURL = links.data["imgURL"].toString(),
                                publisher = links.data["publisher"].toString(),
                                userId = links.data["userId"].toString(),
                                latitude = links.data["latitude"] as Number?,
                                longitude = links.data["longitude"] as Number?,
                                altitude = links.data["altitude"] as Number?,
                                heading = links.data["heading"] as Number?,
                                anchorID = links.data["anchorID"].toString()
                                )
                        )
                    }

                    uploadAdapter.datas = datas
                    uploadAdapter.notifyDataSetChanged()

                }

                val intent = Intent(this, UploadActivity::class.java)

            }
    }
}