package com.google.ar.core.examples.java.app

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.ar.core.examples.java.app.board.upload.UploadUsingPicaActivity
import com.google.ar.core.examples.java.app.map.MapLocationActivity
import com.google.ar.core.examples.java.geospatial.ArLikes
import com.google.ar.core.examples.java.geospatial.ArNav
import com.google.ar.core.examples.java.geospatial.R
import com.google.ar.core.examples.java.retrofit_rest.MapActivity

class Fragment1 : Fragment() {
    val TAG = "Fragment1"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v =  inflater.inflate(R.layout.fragment_first, container, false)
        val uploadButton:Button = v.findViewById(R.id.uploadButton)
        val navButton:Button = v.findViewById(R.id.navButton)
        val maplocationButton:Button = v.findViewById(R.id.maplcationButton)
        val retrofitTester:Button = v.findViewById(R.id.retrofitTester)
        uploadButton.setOnClickListener{
            val nextIntent = Intent(requireContext(), UploadUsingPicaActivity::class.java)
            startActivity(nextIntent)
        }
        // Inflate the layout for this fragment

        navButton.setOnClickListener{
            Intent(context, ArNav::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }

        }

        maplocationButton.setOnClickListener{
            Intent(context, MapLocationActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
        }

        retrofitTester.setOnClickListener{
            Intent(context, MapActivity::class.java).apply {
                putExtra("startLatitude", 37.413003)
                putExtra("startLongitude", 127.125923)
                putExtra("endLatitude", 37.4556093)
                putExtra("endLongitude", 127.1271491)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
        }

//        retrofitTester.setOnClickListener{
//            Intent(context, ArLikes::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }.run { startActivity(this) }
//        }

        return v
    }

}