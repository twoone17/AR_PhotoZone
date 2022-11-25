package com.google.ar.core.examples.java.app.profile.bookmark

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import com.google.ar.core.examples.java.app.board.BoardClickActivity
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_bookmark.*

class BookmarkActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bookmarkAdapter: BookmarkAdapter
    val TAG = "BookmarkActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        initListView()
    }

    private fun initListView() {
        val listView = findViewById<ListView>(R.id.listview1)
        bookmarkAdapter = BookmarkAdapter(this)
        listView.adapter = bookmarkAdapter

        listView.setOnItemClickListener { adapterView, view, position, id ->
            val boardData = bookmarkAdapter.getItem(position)
            // 게시판에서와 동일하게 boardData를 Intent로 BoardPostClickEvent를 호출한다.
            Intent(applicationContext, BoardClickActivity::class.java).apply {
                putExtra("boardData", boardData)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
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
            val savedPostRef = db.collection("users").document(currentUser.uid).collection("BoardSaves")
            val boardPostRef = db.collection("app_board")

            savedPostRef.get().addOnSuccessListener { result ->
                for (savedPostInfo in result) {
                    var postId = savedPostInfo.get("post").toString()
                    boardPostRef.document(postId).get().addOnSuccessListener { documentSnapshot ->
                        val boardDataElement = BoardData(
                            documentSnapshot.get("imgURL").toString(),
                            documentSnapshot.get("description").toString(),
                            documentSnapshot.get("likes") as Long,
                            documentSnapshot.get("publisher").toString(),
                            documentSnapshot.get("userId").toString(),
                            documentSnapshot.get("documentId").toString(),
                        )
                        bookmarkAdapter.addItem(boardDataElement)
                    }
                }

            }.addOnFailureListener { error ->
                Log.e(TAG, "initListView: " + error )
            }
        }
    }
}