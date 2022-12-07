package com.google.ar.core.examples.java.app.board

import android.app.Activity
import android.app.Service
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.ar.core.examples.java.app.board.upload.UploadImageViewActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.log

class UploadActivity : AppCompatActivity() {
    private var auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()
    var imgURL: String? = null
    var placeCluster: String? = null
    val requestCode: Int? = null
    val AUTOCOMPLETE_REQUEST_CODE = 200;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val uploadButton = findViewById<TextView>(R.id.upload)
        val image_added = findViewById<ImageView>(R.id.image_added)
        val close = findViewById<ImageView>(R.id.close)
        var editText = findViewById<EditText>(R.id.description)
        var description = editText.text.toString()

        val uploaddata = intent.getSerializableExtra("uploadData") as BoardData?
        println("uploaddata = ${uploaddata}")
        Log.e(TAG, "onCreate: uploadData" + uploaddata)


        // 키보드 보이기 시, showSoftInput() 호출하기 전 requestFocus() 호출
        editText.requestFocus();

        editText.setOnClickListener {
            fun onClick(v: View?) {
                editText.clearFocus()
                editText.requestFocus()
                val imm: InputMethodManager =
                    this.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, 0)
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        initPlaces()
        initAutoCompleteFragment()

        //사진을 고르면 고른 사진을 띄워준다
        if (uploaddata != null) {
            imgURL = uploaddata!!.imgURL
            Glide.with(this).load(imgURL).error(R.drawable.ic_baseline_error_outline_24)
                .into(image_added)
        }



        uploadButton.setOnClickListener {
            println("!! 업로드 버튼 클릭 !!")
            val now = LocalDateTime.now()
            val documentID =
                now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH))


            val data = BoardData(
                imgURL = imgURL!!,
                description = description,
                publisher = auth.currentUser!!.uid,
                userId = auth.currentUser!!.uid,
                latitude = uploaddata!!.latitude as Number?,
                longitude = uploaddata!!.longitude as Number?,
                altitude = uploaddata!!.altitude as Number?,
                heading = uploaddata!!.heading as Number?,
                documentId = documentID,
                placeCluster = placeCluster!!
            )

            db.collection("app_board").document(documentID).set(data!!)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "게시글 작성완료", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "게시글 작성 실패", Toast.LENGTH_LONG).show()
                }
            //photoZone에 저장
            Log.e(TAG, "onCreate: placeclsuter null 전 " + placeCluster)

            val docData = hashMapOf(
                "imgURL" to uploaddata!!.imgURL!!,
                "latitude" to uploaddata!!.latitude as Number?,
                "longitude" to uploaddata!!.longitude as Number?,
                "altitude" to uploaddata!!.altitude as Number?
            )

//            val postList =
            if (placeCluster != null) {
                //photozone 정보 서버에 업로드
                db.collection("photoZone").document(placeCluster!!)
                    .update(docData as Map<String, Any>)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "게시글 작성완료", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "게시글 작성 실패", Toast.LENGTH_LONG).show()
                    }
                //photozone에서 촬영한 게시글 데이터 업로드
                db.collection("photoZone").document(placeCluster!!)
                    .collection("boardList").document(documentID).set(data)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "게시글 작성완료", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "게시글 작성 실패", Toast.LENGTH_LONG).show()
                    }
            }
        }

        image_added.setOnClickListener {
            println("!! 사진 이미지 클릭 !!")
            val nextIntent = Intent(this, UploadImageViewActivity::class.java)
            startActivity(nextIntent)
        }

        close.setOnClickListener {
            println("!! close 클릭 !!")
            finish()
        }


    }


    // 의성형 하고싶은거 다 해


    // 여기부터는 내가 하고싶은거 다 할게

    private fun initPlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.googleAPIKey))
            Log.e(TAG, "initPlaces: 접근")
        }
    }

    private fun initAutoCompleteFragment() {
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        Log.e(TAG, "initAutoCompleteFragment: autocompleteFragment")
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.e(TAG, "onPlaceSelected: ㅎㅇ")
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
                requestCode == AUTOCOMPLETE_REQUEST_CODE
                placeCluster = place.name

            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Log.i(TAG, "Place: ${place.name}, ${place.id}")
                        placeCluster = place.name

                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, status.statusMessage ?: "에러 발생")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}