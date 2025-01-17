package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.letscouncil.data.UserPreferences
import com.example.letscouncil.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        val userPreferences = UserPreferences(this)
        val user = userPreferences.getUser()

        // Welcome message based on time of day
        binding.welcomeText.text = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> getString(R.string.welcome_morning)
            in 12..16 -> getString(R.string.welcome_afternoon)
            else -> getString(R.string.welcome_evening)
        }

        // Date
        val dateFormat = SimpleDateFormat(getString(R.string.date_format), Locale.getDefault())
        binding.dateText.text = dateFormat.format(Date())

        // Level progress
        if (user != null) {
            val level = (user.score / 100) + 1
            binding.progressText.text = getString(R.string.level_format, level)
            binding.levelProgressBar.progress = user.score % 100
        }
    }

    private fun setupClickListeners() {
        // Write card click listener
        binding.writeCard.setOnClickListener {
            animateCardClick(it)
            startActivity(Intent(this, WriteActivity::class.java))
        }

        // Analysis card click listener
        binding.analysisCard.setOnClickListener {
            animateCardClick(it)
            startActivity(Intent(this, CounselActivity::class.java))
        }

        // Journal card click listener
        binding.journalCard.setOnClickListener {
            animateCardClick(it)
            startActivity(Intent(this, DataActivity::class.java))
        }

        // Chat card click listener
        binding.chatCard.setOnClickListener {
            animateCardClick(it)
            startActivity(Intent(this, AIMenuActivity::class.java))
        }
    }

    private fun animateCardClick(view: View) {
        val scaleDown = AnimationUtils.loadAnimation(this, R.anim.card_click_scale_down)
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.card_click_scale_up)

        view.startAnimation(scaleDown)
        scaleDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                view.startAnimation(scaleUp)
            }
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}