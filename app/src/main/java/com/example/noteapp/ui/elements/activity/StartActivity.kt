//package com.example.noteapp.ui.elements.activity
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import androidx.appcompat.app.AppCompatActivity
//import com.example.noteapp.R
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//
//class StartActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_start)
//
//        val startButton : Button = findViewById(R.id.start_button)
//
//        startButton.setOnClickListener {
//            // check if the user is authenticated or not
//            val isAuthenticatedUser = Firebase.auth.currentUser != null
//            if(isAuthenticatedUser){
//                navigateToMain() // is authenticated go to the Main Activity
//            }else{
//                navigateToAuthentication()  // if not authenticated user go to Authentication Activity
//            }
//        }
//
//
//    }
//
//    // navigate to authentication activity
//    private fun navigateToAuthentication() {
//        val intent = Intent(this, AuthenticationActivity::class.java)
//        startActivity(intent)
//        finish()
//    }
//
//    // navigate to main activity
//    private fun navigateToMain() {
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//        finish()
//    }
//}

package com.example.noteapp.ui.elements.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.noteapp.R
import com.example.noteapp.utils.adapters.OnboardingAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class StartActivity : AppCompatActivity() {

    private lateinit var dots: Array<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        // Khởi tạo dots
        setupDots()

        // Xử lý nút điều hướng
        val startButton: androidx.cardview.widget.CardView = findViewById(R.id.start_button)
        val nextButton: androidx.cardview.widget.CardView = findViewById(R.id.next_button)
        val previousButton: androidx.cardview.widget.CardView = findViewById(R.id.previous_button)
        val proceedButton: androidx.cardview.widget.CardView = findViewById(R.id.proceed_button)

        // Thiết lập trạng thái ban đầu
        updateNavigationButtons(0)
        updateDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateNavigationButtons(position)
                updateDots(position)
            }
        })

        startButton.setOnClickListener {
            viewPager.currentItem = 1
        }

        nextButton.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem += 1
            }
        }

        previousButton.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        proceedButton.setOnClickListener {
            val isAuthenticatedUser = Firebase.auth.currentUser != null
            if (isAuthenticatedUser) {
                navigateToMain()
            } else {
                navigateToAuthentication()
            }
        }
    }

    private fun setupDots() {
        dots = arrayOf(
            findViewById(R.id.dot_0),
            findViewById(R.id.dot_1),
            findViewById(R.id.dot_2)
        )
    }

    private fun updateDots(currentPosition: Int) {
        val activeSize = (12 * resources.displayMetrics.density).toInt() // 12dp to pixels
        val inactiveSize = (8 * resources.displayMetrics.density).toInt() // 8dp to pixels

        for (i in dots.indices) {
            if (i == currentPosition) {
                dots[i].setBackgroundResource(R.drawable.dot_active)
                val params = dots[i].layoutParams
                params.width = activeSize
                params.height = activeSize
                dots[i].layoutParams = params
            } else {
                dots[i].setBackgroundResource(R.drawable.dot_inactive)
                val params = dots[i].layoutParams
                params.width = inactiveSize
                params.height = inactiveSize
                dots[i].layoutParams = params
            }
        }
    }

    private fun updateNavigationButtons(position: Int) {
        val startButton: androidx.cardview.widget.CardView = findViewById(R.id.start_button)
        val nextButton: androidx.cardview.widget.CardView = findViewById(R.id.next_button)
        val previousButton: androidx.cardview.widget.CardView = findViewById(R.id.previous_button)
        val proceedButton: androidx.cardview.widget.CardView = findViewById(R.id.proceed_button)

        when (position) {
            0 -> {
                // Trang đầu tiên
                startButton.visibility = View.VISIBLE
                nextButton.visibility = View.GONE
                proceedButton.visibility = View.GONE
                previousButton.visibility = View.GONE
            }
            1 -> {
                // Trang thứ hai
                startButton.visibility = View.GONE
                nextButton.visibility = View.VISIBLE
                proceedButton.visibility = View.GONE
                previousButton.visibility = View.VISIBLE
            }
            2 -> {
                // Trang cuối cùng
                startButton.visibility = View.GONE
                nextButton.visibility = View.GONE
                proceedButton.visibility = View.VISIBLE
                previousButton.visibility = View.VISIBLE
            }
            else -> {
                // Các trang khác (nếu có thêm)
                startButton.visibility = View.GONE
                nextButton.visibility = if (position < 2) View.VISIBLE else View.GONE
                proceedButton.visibility = if (position >= 2) View.VISIBLE else View.GONE
                previousButton.visibility = if (position > 0) View.VISIBLE else View.GONE
            }
        }
    }

    private fun navigateToAuthentication() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}