package com.google.ar.core.examples.java.app.board

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.java.geospatial.R
import kotlinx.android.synthetic.main.fragment_second.*

class Boardfragment : Fragment() {

    private lateinit var boardAdapter: BoardAdapter
    val datas = mutableListOf<BoardData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_second, container, false)
        initRecycler(v)

        return v
    }

    private fun initRecycler(v : View) {
        boardAdapter = BoardAdapter(this.requireContext())
        val recyclerView: RecyclerView = v.findViewById(R.id.rv_profile)
        recyclerView.adapter = boardAdapter
//        rv_profile.adapter = boardAdapter


        datas.apply {
            add(BoardData(img = R.drawable.kkarmi))
            boardAdapter.datas = datas
            boardAdapter.notifyDataSetChanged()

        }
    }

}