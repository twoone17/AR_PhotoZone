package com.google.ar.core.examples.java.activity

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.ar.core.examples.java.geospatial.R


class HelloActivity : AppCompatActivity() {

    private val frame: RelativeLayout by lazy { // activity_main의 화면 부분
        findViewById(R.id.body_container)
    }
    private val bottomNagivationView: BottomNavigationView by lazy { // 하단 네비게이션 바
        findViewById(R.id.bottom_navigation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)

        // 애플리케이션 실행 후 첫 화면 설정
        supportFragmentManager.beginTransaction().add(frame.id, Fragment1()).commit()

        // 하단 네비게이션 바 클릭 이벤트 설정
        bottomNagivationView.setOnNavigationItemSelectedListener {item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(Fragment1())
                    true
                }
                R.id.nav_board -> {
                    replaceFragment(Fragment2())
                    true
                }
                R.id.nav_search -> {
                    replaceFragment(Fragment3())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(Fragment4())
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
