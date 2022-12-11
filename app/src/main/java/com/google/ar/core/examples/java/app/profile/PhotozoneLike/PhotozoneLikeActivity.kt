package com.google.ar.core.examples.java.app.profile.PhotozoneLike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.google.ar.core.examples.java.app.board.BoardClickActivity
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.app.profile.bookmark.BookmarkAdapter
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class PhotozoneLikeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var photozoneAdapter: PhotozoneLikeAdapter
    val TAG = "BookmarkActivity"
    var likesCount = 2L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photozone_like)
        initListView()
    }

    private fun initListView() {
        val listView = findViewById<ListView>(R.id.listview1)
        photozoneAdapter = PhotozoneLikeAdapter(this)
        listView.adapter = photozoneAdapter

        listView.setOnItemClickListener { adapterView, view, position, id ->
            val boardData = photozoneAdapter.getItem(position)
            // 게시판에서와 동일하게 boardData를 Intent로 BoardPostClickEvent를 호출한다.
//            Intent(applicationContext, BoardClickActivity::class.java).apply {
//                putExtra("boardData", boardData)
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }.run { startActivity(this) }
            // TODO 여기서도 지도에서 포토존 누른 것 처럼 커스텀 다이얼로그?
        }

        getListViewFromServer()
    }

    private fun getListViewFromServer() {
        auth = Firebase.auth
        val currentUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        if(currentUser != null) {
            // users -> uid -> BoardSaves 에서 저장한 게시글 Id를 가져와서,
            // app_board -> (documentId) 로 접근해서 게시글 정보들 받아온 뒤 어뎁터에 뿌려주기
            val savedPostRef = db.collection("users").document(currentUser.uid).collection("photoZoneLikes")
                .document(currentUser.uid)
            val boardPostRef = db.collection("photoZone")

            var photozoneName = "포토존 명";

            savedPostRef.get().addOnSuccessListener { result ->
//                for (savedPostInfo in result) {
//                    var postId = savedPostInfo.get("post").toString()
//                    boardPostRef.document(postId).get().addOnSuccessListener { documentSnapshot ->
//                        val boardDataElement = BoardData(
//                            documentSnapshot.get("imgURL").toString(),
//                            documentSnapshot.get("description").toString(),
//                            documentSnapshot.get("likes") as Long,
//                            documentSnapshot.get("publisher").toString(),
//                            documentSnapshot.get("userId").toString(),
//                            documentSnapshot.get("documentId").toString(),
//                        )
//                        photozoneAdapter.addItem(boardDataElement)
//                    }
//                }
                photozoneName = result.get("photoZoneName") as String
                boardPostRef.document(photozoneName).collection("userLikes").get().addOnSuccessListener { result ->
                    likesCount = result.size().toLong()
                }
                boardPostRef.document(photozoneName).get().addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                val document = task.result
                                val photozoneInfoElement = PhotozoneInfo(
                                    document.get("altitude") as Double,
                                    document.get("latitude") as Double,
                                    document.get("longitude") as Double,
                                    document.get("imgURL") as String,
                                    photozoneName,
                                    likesCount
                                )
                                photozoneAdapter.addItem(photozoneInfoElement)
                            }
                    }
                }
        }
    }
}