package com.google.ar.core.examples.java.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.examples.java.geospatial.R
import com.google.ar.core.examples.java.geospatial.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_bookmark_list_element.*
import kotlinx.android.synthetic.main.activity_map_location.view.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.view.*
import java.util.*


class SignupActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 뒤로가기
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // 회원가입 버튼
        signupButton.setOnClickListener {

            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val email = idEditText.text.toString()
            val pw = passwordEditText.text.toString()
            val pwc = pwcheckEditText.text.toString()

            signup(name, phone, email, pw, pwc)
        }
    }

    //회원가입
    private fun signup(name:String, phone:String, email:String, pw:String, pwc:String) {

        if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty() && pw.isNotEmpty() && pwc.isNotEmpty()) {
            if (pw == pwc) {
                auth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show()

                        var userData = UserData()
                        userData.uid = auth.currentUser?.uid
                        userData.userName = name
                        userData.email = email
                        userData.phoneNumber = phone

                        firestore.collection("users").document(userData.uid.toString()).set(userData)

                    } else { Toast.makeText(this, "Login fail", Toast.LENGTH_SHORT).show() }
                }
            }else { Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show() }
        } else { Toast.makeText(this, "Fill in all the blanks", Toast.LENGTH_SHORT).show() }
    }
}

data class UserData(
    var uid: String? = null,
    var userName: String? = null,
    var email: String? = null,
    var phoneNumber: String? = null,
)