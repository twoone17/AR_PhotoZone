package com.google.ar.core.examples.java.app.profile.PhotozoneLike

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PhotozoneLikeAdapter(val context : Context) : BaseAdapter() {

    private val photozoneInfoList = mutableListOf<PhotozoneInfo>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View =
            LayoutInflater.from(context).inflate(R.layout.activity_photozone_likes_list_element, null)

        val postImg = view.findViewById<ImageView>(R.id.postimg)
        val photozoneDetail = view.findViewById<TextView>(R.id.photozoneDetail)
        val photozoneLikes = view.findViewById<TextView>(R.id.photozoneLikes)
        val name = view.findViewById<TextView>(R.id.photozoneName)


        val postInfo = photozoneInfoList[position]
        Glide.with(view).load(postInfo.imgURL).error(R.drawable.ic_baseline_error_outline_24)
            .into(postImg)

        name.text = postInfo.photozoneName as String

        val positionString = "위도 : " + postInfo.latitude + ", \n경도 : " + postInfo.longitude

//        val likesCount

        photozoneDetail.text = positionString

        photozoneLikes.text = postInfo.photozoneLikes.toString() + " likes"

//        db.collection("users").document(postInfo.userId).get().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val document = task.result
//                name.text = document.get("userName").toString()
//            }
//        }
        return view
    }
fun addItem(data: PhotozoneInfo) {
    photozoneInfoList.add(data)
    notifyDataSetChanged()
}

override fun getItem(position: Int): PhotozoneInfo {
    return photozoneInfoList[position]
}

override fun getItemId(position: Int): Long {
    return 0L
}

override fun getCount(): Int {
    return photozoneInfoList.size
}
}
