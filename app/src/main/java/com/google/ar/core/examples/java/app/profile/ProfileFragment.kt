package com.google.ar.core.examples.java.app.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.ar.core.examples.java.app.board.BoardClickActivity
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.app.profile.bookmark.BookmarkActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var profileAdapter : ProfileAdapter
    private lateinit var auth : FirebaseAuth
    val datas = mutableListOf<BoardData>()
    val TAG = "ProfileFragment"
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    var fileNameProfile : String? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        pressBookmarkButton(v)
        changingProfile(v)
        initImageViewProfile(v)
        initRecycler(v)
        return v
    }

    private fun initImageViewProfile(view: View) {
        val circle_img : CircleImageView = view!!.findViewById(R.id.circle_img)


        circle_img.setOnClickListener {
            when {
                // 갤러리 접근 권한이 있는 경우
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                -> {
                    navigateGallery()
                }

                // 갤러리 접근 권한이 없는 경우 & 교육용 팝업을 보여줘야 하는 경우
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                -> {
                    showPermissionContextPopup()
                }

                // 권한 요청 하기(requestPermissions) -> 갤러리 접근(onRequestPermissionResult)
                else -> requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                )
            }

        }
    }

    // 권한 요청 승인 이후 실행되는 함수
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    navigateGallery()
                else
                    Toast.makeText(requireContext(), "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                //
            }
        }
    }

    private fun navigateGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        // 가져올 컨텐츠들 중에서 Image 만을 가져온다.
        intent.type = "image/*"
        // 갤러리에서 이미지를 선택한 후, 프로필 이미지뷰를 수정하기 위해 갤러리에서 수행한 값을 받아오는 startActivityForeResult를 사용한다.
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 예외처리
        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            // 2000: 이미지 컨텐츠를 가져오는 액티비티를 수행한 후 실행되는 Activity 일 때만 수행하기 위해서
            2000 -> {
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    val ref = storageReference?.child("myProfile/${auth.currentUser!!.uid}")
                    val uploadTask = ref?.putFile(selectedImageUri!!)

                    val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@Continuation ref.downloadUrl
                    })?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                        } else {
                            // Handle failures
                        }
                    }?.addOnFailureListener {

                    }
                } else {
                    Toast.makeText(requireContext(), "Please Upload an Image", Toast.LENGTH_SHORT).show()
                }
                circle_img.setImageURI(selectedImageUri)

            }
            else -> {
                Toast.makeText(requireContext(), "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(requireContext())
                .setTitle("권한이 필요합니다.")
                .setMessage("프로필 이미지를 바꾸기 위해서는 갤러리 접근 권한이 필요합니다.")
                .setPositiveButton("동의하기") { _, _ ->
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
                .setNegativeButton("취소하기") { _, _ -> }
                .create()
                .show()
    }


    private fun changingProfile(view : View) {
        val circle_img : CircleImageView = view.findViewById(R.id.circle_img)
        circle_img.setOnClickListener {
            val nextIntent = Intent(requireContext(), BookmarkActivity::class.java)
            startActivity(nextIntent)
        }
    }

    private fun pressBookmarkButton(view : View) {
        val bookmarkButton : ImageButton = view.findViewById(R.id.bookmarkButton)
        bookmarkButton.setOnClickListener {
            val nextIntent = Intent(requireContext(), BookmarkActivity::class.java)
            startActivity(nextIntent)
        }
    }

    private fun initRecycler(view : View) {
        profileAdapter = ProfileAdapter(this.requireContext())
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_mypost)

        profileAdapter.setOnItemClickListener(object : ProfileAdapter.OnItemClickListener{
            override fun onItemClick(view: View, boardData: BoardData, position: Int) {
                Intent(requireContext(), BoardClickActivity::class.java).apply {
                    putExtra("boardData", boardData)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        })

        recyclerView.adapter = profileAdapter
        val gm = GridLayoutManager(view.context, 2)
        recyclerView.layoutManager = gm

        initRecyclerData(profileAdapter)
    }

    /**
     * 마이페이지 오류 : 게시판 db를 지웠는데
     * users - user 이름 - MyBoard안에 있는 postID를 지우지 않아서 null 오류 발생
     * 해결 방법 : 게시글 지울때 myboard 안에있는것도 같이 지워줘야함
     */
    private fun initRecyclerData(profileAdapter: ProfileAdapter) {
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val boardRef = db.collection("app_board")
            db.collection("users").document(currentUser.uid).collection("MyBoard").get().addOnSuccessListener { result ->
                for (myPost in result) {
                    boardRef.document(myPost.data["postId"].toString()).get().addOnSuccessListener { result ->
                        datas.apply {
                            add(BoardData(imgURL = result.get("imgURL").toString(),
                                description = result.get("description").toString(),
                                likes = result.get("likes") as Long?,
                                publisher = result.get("publisher").toString(),
                                userId = result.get("userId").toString(),
                                documentId = result.id))
                            profileAdapter.datas = datas
                            profileAdapter.notifyDataSetChanged()
                        }
                    }
                }

            }.addOnFailureListener { error ->
                Log.e(TAG, "initRecyclerData: " + error)
            }

            //프로필 사진 불러오기
//            val pathReference = storageReference!!.child("gs://sceneform-android1.appspot.com/myProfile/${currentUser.uid}")
            val pathReference = storageReference!!.child("myProfile/${currentUser.uid}")
            pathReference.downloadUrl.addOnSuccessListener { uri ->
//                circle_img.setImageURI(uri)
                Glide.with(requireContext()).load(uri).error(R.drawable.ic_baseline_error_outline_24).centerCrop().into(circle_img)
            }

        }
    }

}