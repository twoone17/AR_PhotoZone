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
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.ar.core.examples.java.app.board.BoardClickActivity
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.app.profile.PhotozoneLike.PhotozoneLikeActivity
import com.google.ar.core.examples.java.app.profile.bookmark.BookmarkActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
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
    private lateinit var db : FirebaseFirestore
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
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        storageReference = FirebaseStorage.getInstance().reference
        pressBookmarkButton(v)
        initUserName(v)
        initlikedPhotozoneButton(v)
        initImageViewProfile(v)
        initRecycler(v)
        return v
    }

    private fun initUserName(view: View) {
        val userNickname = view.findViewById<TextView>(R.id.userNickname)
        db.collection("users").document(auth.currentUser!!.uid).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val document = task.result
                userNickname.text = document.get("userName").toString()
            }
        }
    }

    private fun initlikedPhotozoneButton(view : View) {
        val likedPhotozoneButton = view.findViewById<ImageButton>(R.id.likedPhotozoneButton)
        likedPhotozoneButton.setOnClickListener {
            val nextIntent = Intent(requireContext(), PhotozoneLikeActivity::class.java)
            startActivity(nextIntent)
        }
    }

    private fun initImageViewProfile(view: View) {
        val circle_img : CircleImageView = view!!.findViewById(R.id.circle_img)


        circle_img.setOnClickListener {
            when {
                // ????????? ?????? ????????? ?????? ??????
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                -> {
                    navigateGallery()
                }

                // ????????? ?????? ????????? ?????? ?????? & ????????? ????????? ???????????? ?????? ??????
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                -> {
                    showPermissionContextPopup()
                }

                // ?????? ?????? ??????(requestPermissions) -> ????????? ??????(onRequestPermissionResult)
                else -> requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                )
            }

        }
    }

    // ?????? ?????? ?????? ?????? ???????????? ??????
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
                    Toast.makeText(requireContext(), "????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                //
            }
        }
    }

    private fun navigateGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        // ????????? ???????????? ????????? Image ?????? ????????????.
        intent.type = "image/*"
        // ??????????????? ???????????? ????????? ???, ????????? ??????????????? ???????????? ?????? ??????????????? ????????? ?????? ???????????? startActivityForeResult??? ????????????.
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // ????????????
        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            // 2000: ????????? ???????????? ???????????? ??????????????? ????????? ??? ???????????? Activity ??? ?????? ???????????? ?????????
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
                Toast.makeText(requireContext(), "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(requireContext())
                .setTitle("????????? ???????????????.")
                .setMessage("????????? ???????????? ????????? ???????????? ????????? ?????? ????????? ???????????????.")
                .setPositiveButton("????????????") { _, _ ->
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
                .setNegativeButton("????????????") { _, _ -> }
                .create()
                .show()
    }


//    private fun changingProfile(view : View) {
//        val circle_img : CircleImageView = view.findViewById(R.id.circle_img)
//        circle_img.setOnClickListener {
//            val nextIntent = Intent(requireContext(), BookmarkActivity::class.java)
//            startActivity(nextIntent)
//        }
//    }

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
     * ??????????????? ?????? : ????????? db??? ????????????
     * users - user ?????? - MyBoard?????? ?????? postID??? ????????? ????????? null ?????? ??????
     * ?????? ?????? : ????????? ????????? myboard ?????????????????? ?????? ???????????????
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

            //????????? ?????? ????????????
//            val pathReference = storageReference!!.child("gs://sceneform-android1.appspot.com/myProfile/${currentUser.uid}")
            val pathReference = storageReference!!.child("myProfile/${currentUser.uid}")
            pathReference.downloadUrl.addOnSuccessListener { uri ->
//                circle_img.setImageURI(uri)
                Glide.with(requireContext()).load(uri).error(R.drawable.ic_baseline_error_outline_24).centerCrop().into(circle_img)
            }

        }
    }

}