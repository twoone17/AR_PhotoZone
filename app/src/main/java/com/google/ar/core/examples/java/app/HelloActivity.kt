package com.google.ar.core.examples.java.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.ar.core.examples.java.app.board.Boardfragment
import com.google.ar.core.examples.java.app.profile.ProfileFragment
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


// branch test
class HelloActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    private val frame: RelativeLayout by lazy { // activity_main의 화면 부분
        findViewById(R.id.body_container)
    }
    private val bottomNagivationView: BottomNavigationView by lazy { // 하단 네비게이션 바
        findViewById(R.id.bottom_navigation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)

        if(ActivityCompat.checkSelfPermission(this.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this.baseContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            //ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), REQEST_CODE)
            //return
        }


        auth = Firebase.auth
        // 임시로 일단 로그인을 시작과 동시에 시켜놓겠음
        // 뒤에서 인증 때문에 자꾸 구현이 막힌다
//        auth.signInWithEmailAndPassword("oldstyle4@naver.com", "2580as2580@")
//        auth.signInWithEmailAndPassword("euisung@naver.com", "2580as2580@")

        // 애플리케이션 실행 후 첫 화면 설정
        supportFragmentManager.beginTransaction().add(frame.id, Fragment1()).commit()

        // 하단 네비게이션 바 클릭 이벤트 설정
        bottomNagivationView.setOnNavigationItemSelectedListener {item ->
            when(item.itemId) {
//                R.id.nav_home -> {
//                    replaceFragment(Fragment1())
//                    true
//                }
                R.id.nav_board -> {
                    replaceFragment(Boardfragment())
                    true
                }
                R.id.nav_search -> {
                    replaceFragment(Fragment3())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    // 화면 전환 구현 메소드
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(frame.id, fragment).commit()
    }
}
