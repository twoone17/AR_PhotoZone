package com.google.ar.core.examples.java.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.examples.java.app.board.UploadActivity
import com.google.ar.core.examples.java.app.board.upload.UploadUsingPicaActivity
import com.google.ar.core.examples.java.geospatial.R
import kotlinx.android.synthetic.main.fragment_first.*

class Fragment1 : Fragment() {
    val TAG = "Fragment1"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v =  inflater.inflate(R.layout.fragment_first, container, false)
        val uploadButton:Button = v.findViewById(R.id.uploadButton)

        uploadButton.setOnClickListener{
            val nextIntent = Intent(requireContext(), UploadUsingPicaActivity::class.java)
            startActivity(nextIntent)
        }
        // Inflate the layout for this fragment
        return v
    }

}