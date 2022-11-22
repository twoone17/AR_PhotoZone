package com.google.ar.core.examples.java.app.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_board_click.*

class BoardClickActivity : AppCompatActivity() {

    val TAG = "BoardClickActivity"
    private lateinit var auth: FirebaseAuth
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_click)

        val intent = intent
        val boardData = intent.getSerializableExtra("boardData") as BoardData?
        if(boardData != null) {

            initBoardWithIntentData(boardData)
            // 좋아요를 헤당 게시글에 이미 눌렀는지 확인해주는 함수
            isLikePressed(boardData)
            // 좋아요 버튼 클릭에 대해 처리하는 함수
            pressLikeButton()
            // 저장 버튼 클릭에 대해 처리하는 함수
            pressSaveButton()
        }
    }

    // 인텐트로 넘어온 데이터를 뷰에 뿌려주는 함수
    private fun initBoardWithIntentData(boardData: BoardData) {
            Glide.with(this).load(boardData.img).error(R.drawable.ic_baseline_error_outline_24).into(post_image)
            username.text = boardData.userId
            description.text = boardData.description
            publisher.text = boardData.userId
            var likeString = boardData.likes.toString() + " likes"
            likes.text = likeString
            likes.text = likeString
    }

    private fun isLikePressed(boardData: BoardData) {
        auth = Firebase.auth
        // 임시 자동 로그인, 로그인 화면 구현 시 삭제 예정
        auth.signInWithEmailAndPassword("oldstyle4@naver.com", "2580as2580@").addOnSuccessListener {
            val currentUser = auth.currentUser
            if(currentUser != null) {
                val uid = currentUser.uid
                var likesReference : CollectionReference = db.collection("app_board").document(boardData.documentId).collection("Likes")
                likesReference.document(uid).get().addOnSuccessListener { result ->
                    Log.e(TAG, "isLikePressed: " + result.reference)
                }
            }
        }
        // 현재 유저가 null이 아니어야 모든 로직이 실행될 수 있다.
    }

    private fun pressLikeButton() {
        like.setOnClickListener{

        }
    }

    private fun pressSaveButton() {

    }
}