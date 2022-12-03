package com.google.ar.core.examples.java.app.board.upload

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.app.board.UploadActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_get_image.*
import kotlinx.android.synthetic.main.activity_upload.*
import kotlinx.android.synthetic.main.upload_main_recycler.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

/**
 * 실제 Photozone Helper를 통해 카메라 촬영후 업로드 하는 activity
 * storage uri가 필요해서 일단 임시로 구현, 갤러리에서 선택하는 형식
 */

class UploadUsingPicaActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_image)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = Firebase.auth

        auth.signInWithEmailAndPassword("euisung@naver.com", "2580as2580@")

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        btn_choose_image.setOnClickListener { launchGallery() }
        btn_upload_image.setOnClickListener { uploadImage() }
    }

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                image_preview.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun addUploadRecordToDb(uri: String){
        val db = FirebaseFirestore.getInstance()


//        val data = HashMap<String, Any>()
//        data["imageUrl"] = uri
        val data = BoardData(imgURL = uri,
                description = "업로드 연습용 description1",
                likes = 5,
                publisher = auth.currentUser!!.uid,
                userId = auth.currentUser!!.uid,
                "documentid")

        db.collection("users").document(auth.currentUser!!.uid).collection("posts")
                .add(data)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Saved to DB", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving to DB", Toast.LENGTH_LONG).show()
                }
    }

    private fun uploadImage(){
        if(filePath != null){
            val ref = storageReference?.child("Gallery/${auth.currentUser!!.uid}/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)

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
                    addUploadRecordToDb(downloadUri.toString())
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

}