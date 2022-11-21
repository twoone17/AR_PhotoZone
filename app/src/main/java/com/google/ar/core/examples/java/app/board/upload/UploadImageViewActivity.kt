package com.google.ar.core.examples.java.app.board.upload

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
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
    val datas = mutableListOf<UploadData>()


    //파이어베이스 storage 접근
//    lateinit var storage: FirebaseStorage
    // Create a storage reference from our app

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_main_recycler)
        var listManager = GridLayoutManager(this, 2)
        upload_main_recycler.layoutManager = listManager

        initRecycler()

    }
    private fun initRecycler() {
        uploadAdapter = UploadAdapter(this)
        upload_main_recycler.adapter = uploadAdapter

//        var storageRef = storage.reference
        //userid에 해당하는 갤러리를 불러온다
        //TODO:의성) 추후에 실제 uid로 변경
//        var imagesRef: StorageReference = storageRef.child("Gallery/userid/picID1.png")


        datas.apply {
            add(UploadData(img = R.drawable.kkarmi))
            add(UploadData(img = R.drawable.kkarmi))
            add(UploadData(img = R.drawable.kkarmi))

            uploadAdapter.datas = datas
            uploadAdapter.notifyDataSetChanged()

        }
    }
}