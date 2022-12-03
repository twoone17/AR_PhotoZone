package com.google.ar.core.examples.java.login

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.ar.core.examples.java.geospatial.R
import com.google.ar.core.examples.java.geospatial.databinding.ActivitySignupBinding
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.view.*
import java.util.*

class SignupActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        val auth = FirebaseAuth.getInstance()

        val name = binding.nameEditText.text.toString()
        val phone = binding.phoneEditText.text.toString()
        val email = binding.idEditText.text.toString()
        val pw = binding.passwordEditText.text.toString()
        val pwc = binding.pwcheckEditText.text.toString()

        // 뒤로가기
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
        }

        // 회원가입 버튼
        signupButton.setOnClickListener {
            signup(name, phone, email, pw, pwc)
        }
    }

    //회원가입
    private fun signup(name:String, phone:String, email:String, pw:String, pwc:String) {

        //데이터 hashMap에
        val firebaseUser: FirebaseUser? = auth!!.currentUser
        val userid = firebaseUser?.uid
        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(userid!!)
        val hashMap: HashMap<String, Any> = HashMap()

        hashMap["id"] = userid
        hashMap["username"] = name.lowercase(Locale.getDefault())
        hashMap["phone"] = phone
        hashMap["email"] = email
        hashMap["pw"] = pw

        if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty() && pw.isNotEmpty() && pwc.isNotEmpty()) {
            if (pw == pwc) {
                auth?.createUserWithEmailAndPassword(email,pw)
                //firebase데이터베이스에 유저 추가 (setValue 이용)
                reference.setValue(hashMap).addOnCompleteListener(object: OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if (p0.isSuccessful) {
                            Toast.makeText(this@SignupActivity, "가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                })
            }else { Toast.makeText(this@SignupActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show() }
        } else { Toast.makeText(this@SignupActivity, "빈칸을 다 채워주세요.", Toast.LENGTH_SHORT).show() }
    }
}