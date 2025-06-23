package com.example.mypiggybank.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.mypiggybank.R
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Find views
        val piggyAnimation = findViewById<LottieAnimationView>(R.id.piggyBankAnimation)
        val title = findViewById<TextView>(R.id.welcomeTitle)
        val subtitle = findViewById<TextView>(R.id.welcomeSubtitle)
        val getStartedButton = findViewById<MaterialButton>(R.id.getStartedButton)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 1000

        // Apply animations with delays
        title.startAnimation(fadeIn)
        subtitle.startAnimation(fadeIn)
        
        // Initially hide the button
        getStartedButton.alpha = 0f
        
        // Animate button appearance
        getStartedButton.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(500)
            .start()

        // Set click listener for the button
        getStartedButton.setOnClickListener {
            // Create scale down animation for button click effect
            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            // Navigate to MainActivity
                            startActivity(Intent(this, MainActivity::class.java))
                            // Add slide transition
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            finish()
                        }
                }
                .start()
        }
    }
} 