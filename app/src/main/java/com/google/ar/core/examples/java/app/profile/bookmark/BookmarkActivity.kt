package com.google.ar.core.examples.java.app.profile.bookmark

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class BookmarkActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bookmarkAdapter: BookmarkAdapter
    var savedBoardList = mutableListOf<BoardData>()
    val TAG = "BookmarkActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        initListView()
    }

    private fun initListView() {
        auth = Firebase.auth
        val currentUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        if(currentUser != null) {
            // users -> uid -> BoardSaves 에서 저장한 게시글 Id를 가져와서,
            // app_board -> (documentId) 로 접근해서 게시글 정보들 받아온 뒤 어뎁터에 뿌려주기
            val savedPostRef = db.collection("users").document(currentUser.uid).collection("BoardSaves")
            val boardPostRef = db.collection("app_board")

            savedPostRef.get().addOnSuccessListener { result ->
                for (savedPostInfo in result) {
                    var postId = savedPostInfo.get("post").toString()

                }
                Log.e(TAG, "initListView: " + savedBoardList.size)
            }.addOnFailureListener { error ->
                Log.e(TAG, "initListView: " + error )
            }
        }
    }
}