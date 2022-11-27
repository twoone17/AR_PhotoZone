package com.google.ar.core.examples.java.app.board.comment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.app.board.BoardClickActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_board_click.*
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.time.LocalDateTime
import kotlin.math.log

class CommentActivity : AppCompatActivity() {

    val TAG = "CommentActivity"
    private var auth = Firebase.auth
    private val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    private lateinit var commentAdapter: CommentAdapter
    val datas = mutableListOf<CommentData>()
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        val intent = intent
        val documentId = intent.getStringExtra("post_document_Id").toString()
        val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        initCommentEditText(inputMethodManager)
        initRecycler(documentId)
        pressCommentPushButton(documentId)
    }

    private fun initCommentEditText(inputMethodManager: InputMethodManager) {
        add_comment.setOnClickListener {
            inputMethodManager.showSoftInput(add_comment, 0)
            inputMethodManager.showSoftInput(add_comment, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun initRecycler(documentId: String) {

        commentAdapter = CommentAdapter(this)
        val recyclerView: RecyclerView = findViewById(R.id.comment_list_recycler_view)

        commentAdapter.setOnItemClickListener(object : CommentAdapter.OnItemClickListener{
            override fun onItemClick(view: View, commentData: CommentData, position: Int) {

                Intent(applicationContext, BoardClickActivity::class.java).apply {
                    putExtra("commentData", commentData)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }

            }
        })

        recyclerView.adapter = commentAdapter
        val gm = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = gm

        readCommentsFromServer(documentId, commentAdapter)
        readProfileImgFromServer()
    }

    private fun readProfileImgFromServer() {
        val comment_profile_img : CircleImageView = findViewById(R.id.image_profile)

        if(currentUser!=null)
        {

            //boardData에서 프로필 이미지 띄우기
            println("currentUser = ${currentUser.uid}")
            val pathReference = storageReference!!.child("myProfile/${currentUser.uid}")
            pathReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this).load(uri).error(R.drawable.ic_baseline_error_outline_24).centerCrop().into(comment_profile_img)
            }
    }}


    // 서버로부터 댓글들을 읽어오는 함수
    private fun readCommentsFromServer(documentId: String, commentAdapter: CommentAdapter) {
        val commentReference: CollectionReference =
            db.collection("app_board").document(documentId).collection("Comments")

        commentReference.get().addOnSuccessListener { result ->
            for (commentDocuments in result) {
                datas.apply {
                    add(
                        CommentData(
                            comment = commentDocuments.data["comment"].toString(),
                            uid = commentDocuments.data["uid"].toString(),
                            posted_time = commentDocuments.data["posted_time"].toString()
                        )
                    )
                    commentAdapter.datas = datas
                    commentAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun pressCommentPushButton(documentId : String) {
        post.setOnClickListener {
            if(add_comment.text.toString().equals("")) {
                Toast.makeText(this, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                addComment(documentId, add_comment.text.toString())
            }
        }
    }

    private fun addComment(documentId: String, commentText : String) {
        if(currentUser != null) {
            val commentMap = HashMap<String, String>()
            val localDateTime = LocalDateTime.now()
            val commentReference : CollectionReference =
                db.collection("app_board").document(documentId).collection("Comments")

            commentMap.put("comment", commentText)
            commentMap.put("uid", currentUser.uid)
            commentMap.put("posted_time", localDateTime.toString())

            commentReference.document(currentUser.uid + "&&&" + localDateTime.toString()).set(commentMap).addOnSuccessListener {
               refreshActivity()
                add_comment.setText("")
            }.addOnFailureListener { error ->
                Log.e(TAG, "addComment: " + error)
            }
        }
    }

    // 추가된 댓글을 반영하기 위한 새로고침 함수
    private fun refreshActivity() {
        finish()
        overridePendingTransition(0, 0)
        val intent = intent
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}