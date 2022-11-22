package com.google.ar.core.examples.java.app.board

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_board.*

class Boardfragment : Fragment() {

    private lateinit var boardAdapter: BoardAdapter
    val datas = mutableListOf<BoardData>()
    val TAG = "BoardFragment"
    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v =  inflater.inflate(R.layout.fragment_board, container, false)
        // 리사이클러뷰 초기화 -> 파이어베이스 데이터 로드
        initRecycler(v)

        val uploadButton : FloatingActionButton = v.findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener{
            val nextIntent = Intent(requireContext(), UploadActivity::class.java)
            startActivity(nextIntent)
        }

        return v
    }

    private fun initRecycler(v : View) {
        boardAdapter = BoardAdapter(this.requireContext())
        val recyclerView: RecyclerView = v.findViewById(R.id.recyclerView_BoardItem)

        boardAdapter.setOnItemClickListener(object : BoardAdapter.OnItemClickListener{
            override fun onItemClick(view: View, boardData: BoardData ,position: Int) {
                Intent(requireContext(), BoardClickActivity::class.java).apply {
                    putExtra("boardData", boardData)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        })

        recyclerView.adapter = boardAdapter
        val gm = GridLayoutManager(v.context, 2)
        recyclerView.layoutManager = gm

        initRecyclerData(boardAdapter)
    }

    // 초기화된 리사이클러뷰에 들어갈 데이터를 받아와서 넣어주는 함수
    private fun initRecyclerData(boardAdapter: BoardAdapter) {

        db.collection("app_board").get().addOnSuccessListener { result ->
            for (links in result) {
                datas.apply {
                    add(BoardData(imgURL = links.data["imgURL"].toString(),
                        description = links.data["description"].toString(),
                        likes = links.data["likes"] as Long,
                        publisher = links.data["publisher"].toString(),
                        userId = links.data["userId"].toString(),
                        links.id))
                    boardAdapter.datas = datas
                    boardAdapter.notifyDataSetChanged()
                }
            }

        }.addOnFailureListener { exception ->
                Log.e(TAG, "error on loading datas")
                Log.e(TAG, "content : ", exception)
            }
    }


}

