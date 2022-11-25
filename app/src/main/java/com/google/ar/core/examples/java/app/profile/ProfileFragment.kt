package com.google.ar.core.examples.java.app.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.java.app.board.BoardClickActivity
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var profileAdapter : ProfileAdapter
    private lateinit var auth : FirebaseAuth
    val datas = mutableListOf<BoardData>()
    val TAG = "ProfileFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_profile, container, false)
        initRecycler(v)
        return v
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
                                likes = result.get("likes") as Long,
                                publisher = result.get("publisher").toString(),
                                userId = result.get("userId").toString(),
                                result.id))
                            profileAdapter.datas = datas
                            profileAdapter.notifyDataSetChanged()
                        }
                    }
                }

            }.addOnFailureListener { error ->
                Log.e(TAG, "initRecyclerData: " + error)
            }
        }
    }

}