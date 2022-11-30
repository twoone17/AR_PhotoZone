package com.google.ar.core.examples.java.app.board

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.app.board.comment.CommentActivity
import com.google.ar.core.examples.java.geospatial.GeospatialActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_board_click.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlin.math.log

class BoardClickActivity : AppCompatActivity() {

    val TAG = "BoardClickActivity"
//    private lateinit var auth: FirebaseAuth
    private var auth = Firebase.auth
    private val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null


    var liked = true
    var saved = true
    var likeCount = 0L

    // 좋아요 데이터 삽입용 해쉬맵
    var likeMap = HashMap<String, String>()
    // users -> uid -> BoardLikes -> ... 에 들어갈 게시글 정보를 담기 위한 해쉬맵
    var likePostInfoMap = HashMap<String, String>()
    // 저장 게시글 데이터 삽입용 해쉬맵
    var saveMap = HashMap<String, String>()
    // users -> uid -> BoardSaves -> ... 에 들어갈 게시글 정보를 담기 위한 해쉬맵
    var savePostInfoMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_click)

        val intent = intent
        val boardData = intent.getSerializableExtra("boardData") as BoardData?
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        if(boardData != null) {

            initBoardWithIntentData(boardData)
            // 서버로부터 좋아요 개수를 받아오는 함수
            getLikeCountsFromServer(boardData)
            // 좋아요를 헤당 게시글에 이미 눌렀는지 확인해주는 함수
            isLikePressed(boardData)
            // 좋아요 버튼 클릭에 대해 처리하는 함수
            pressLikeButton(boardData)
            // 해당 게시글을 이미 저장했는지 확인해주는 함수
            isSavePressed(boardData)
            // 저장 버튼 클릭에 대해 처리하는 함수
            pressSaveButton(boardData)
            // 댓글 버튼들 (더보기, 댓글 아이콘) 클릭에 대해 처리하는 함수
            pressCommentButtons(boardData)
        }
    }

    // 인텐트로 넘어온 데이터를 뷰에 뿌려주는 함수
    private fun initBoardWithIntentData(boardData: BoardData) {
            Glide.with(this).load(boardData.imgURL).error(R.drawable.ic_baseline_error_outline_24).into(post_image)
            username.text = boardData.userId
            description.text = boardData.description
            publisher.text = boardData.userId
            var likeString = boardData.likes.toString() + " likes"
            likes.text = likeString


        if(currentUser!=null)
        {
        //boardData에서 프로필 이미지 띄우기
        val pathReference = storageReference!!.child("myProfile/${boardData.publisher}")
        pathReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).error(R.drawable.ic_baseline_error_outline_24).centerCrop().into(image_profile)
        }
        }
    }

    // 좋아요를 헤당 게시글에 이미 눌렀는지 확인해주는 함수
    private fun isLikePressed(boardData: BoardData) {
        // 임시 자동 로그인, 로그인 화면 구현 시 삭제 예정
            if(currentUser != null) {
                val uid = currentUser.uid
                var likesReference : CollectionReference = db.collection("app_board").document(boardData.documentId).collection("Likes")
                likesReference.document(uid).get().addOnSuccessListener { result ->
                    if(result.exists()) {
                        like.setImageResource(R.drawable.ic_liked)
                        liked = false
                    } else {
                        like.setImageResource(R.drawable.ic_like)
                        liked = true
                    }
                }.addOnFailureListener { error ->
                    Log.e(TAG, "isLikePressed : " + error )
                }
            } else {
                liked = false
            }
    }

    // 서버로부터 좋아요 개수를 받아오는 함수
    private fun getLikeCountsFromServer(boardData: BoardData){
        var likesReference : CollectionReference = db.collection("app_board").document(boardData.documentId).collection("Likes")

        likesReference.get().addOnCompleteListener{result ->
            likeCount = 0
            for (documentSnapshot in result.getResult()) {
                // 서버에서 좋아요 수를 받아와서 더해준다.
                likeCount++
            }
            val likeString = "$likeCount likes"
            likes.text = likeString
        }.addOnFailureListener { error ->
            Log.e(TAG, "getLikeCountsFromServer: " + error )
        }

    }

    // 좋아요 버튼 클릭에 대해 처리하는 함수
    private fun pressLikeButton(boardData : BoardData) {
        if(currentUser != null) {
            var uid = currentUser.uid
            var likesReference: CollectionReference =
                db.collection("app_board").document(boardData.documentId).collection("Likes")
            var forUpdateLikesCountDocument: DocumentReference =
                db.collection("app_board").document(boardData.documentId)
            var forModifingBoardLikesCollection: CollectionReference =
                db.collection("users").document(uid).collection("BoardLikes")

                like.setOnClickListener {
                    if (liked) {
                        like.setImageResource(R.drawable.ic_liked)
                        likeCount++
                        likeMap.put("user", uid)
                        likesReference.document(uid).set(likeMap)
                        likePostInfoMap.put("post", boardData.documentId)
                        forModifingBoardLikesCollection.document(boardData.documentId).set(likePostInfoMap)
                        likes.text = "$likeCount likes"
                        boardData.likes = likeCount
                        forUpdateLikesCountDocument.set(boardData)
                        liked = false
                    } else {
                        like.setImageResource(R.drawable.ic_like)
                        likesReference.document(uid).delete()
                        likeCount--
                        likes.text = "$likeCount likes"
                        boardData.likes = likeCount
                        forUpdateLikesCountDocument.set(boardData)
                        // 좋아요 누른 게시글에서 삭제
                        forModifingBoardLikesCollection.document(boardData.documentId).delete()
                        liked = true
                    }
                }
        }
    }


    // 해당 게시글을 이미 저장했는지 확인해주는 함수
    private fun isSavePressed(boardData: BoardData) {
        if(currentUser != null) {
            val uid = currentUser.uid
            var savesReference : CollectionReference = db.collection("app_board").document(boardData.documentId).collection("Saves")
            savesReference.document(uid).get().addOnSuccessListener { result ->
                if(result.exists()) {
                    save.setImageResource(R.drawable.ic_save_black)
                    saved = false
                } else {
                    save.setImageResource(R.drawable.ic_savee_black)
                    saved = true
                }
            }
        } else {
            saved = false
        }
    }

    // 저장 버튼 클릭에 대해 처리하는 함수
    private fun pressSaveButton(boardData: BoardData) {

        if(currentUser != null) {
            var uid = currentUser.uid
            var savesReference: CollectionReference =
                db.collection("app_board").document(boardData.documentId).collection("Saves")
            var forModifingBoardSavesCollection: CollectionReference =
                db.collection("users").document(uid).collection("BoardSaves")


            save.setOnClickListener {
                if (saved) {
                    save.setImageResource(R.drawable.ic_save_black)
                    saveMap.put("user", uid)
                    savesReference.document(uid).set(saveMap)
                    savePostInfoMap.put("post", boardData.documentId)
                    forModifingBoardSavesCollection.document(boardData.documentId).set(savePostInfoMap)
                    saved = false
                } else {
                    save.setImageResource(R.drawable.ic_savee_black)
                    savesReference.document(uid).delete()
                    forModifingBoardSavesCollection.document(boardData.documentId).delete()
                    saved = true
                }
            }
        }
    }

    private fun pressCommentButtons(boardData: BoardData) {
        // "댓글 더보기" 버튼
        comments.setOnClickListener {
            // Intent로 현재 게시글 id 넘기기
            Intent(this, CommentActivity::class.java).apply {
                putExtra("post_document_Id", boardData.documentId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
        }

        // 댓글 아이콘 버튼
        comment.setOnClickListener {
            Intent(this, CommentActivity::class.java).apply {
                putExtra("post_document_Id", boardData.documentId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
        }
    }

    //camera helper 버튼 클릭시
    private fun pressCameraButton(boardData: BoardData) {
        if(currentUser != null) {
            val uid = currentUser.uid
            camera_helper2.setOnClickListener{
                 println("camera click")
                 Intent(this,GeospatialActivity::class.java).apply {
                     putExtra("post_document_Id", boardData.documentId)
                     addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                 }.run { startActivity(this) }

             }
            }

    }

}