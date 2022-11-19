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
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.firestore.FirebaseFirestore

class Boardfragment : Fragment() {

    private lateinit var boardAdapter: BoardAdapter
    val datas = mutableListOf<BoardData>()
    val TAG = "BoardFragment"
    var imgURLs = mutableListOf<String>()
    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v =  inflater.inflate(R.layout.fragment_board, container, false)
        initRecycler(v)
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
                    add(BoardData(img = links.data["imgURL"].toString(),
                        "테스트용 게시글입니다.",
                        2,
                        "2BXzuCaFIYXf7Dp06sHMCrTNSH43",
                        "Iron_Woong"))
                    boardAdapter.datas = datas
                    boardAdapter.notifyDataSetChanged()
                }
                // 자꾸 리스트가 listener를 나가면 초기화된다.
                imgURLs.apply { add(links.data["imgURL"].toString()) }
            }
            println(imgURLs.size)
        }.addOnFailureListener { exception ->
                Log.e(TAG, "error on loading datas")
                Log.e(TAG, "content : ", exception)
            }
    }


}

