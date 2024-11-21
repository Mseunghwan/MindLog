package com.example.letscouncil

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.letscouncil.databinding.ActivityAnalysisBinding
import com.example.letscouncil.databinding.ActivityMainBinding

class AnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val binding = ActivityAnalysisBinding.inflate(layoutInflater)

        setContentView(binding.root)


        val intent = Intent(this, DataActivity::class.java)

        binding.btn.setOnClickListener{
            startActivity(intent)
        }
    }
}