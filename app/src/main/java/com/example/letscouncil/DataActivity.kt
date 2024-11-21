package com.example.letscouncil


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.letscouncil.databinding.ActivityDataBinding

class DataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        /*
        val chartEntry = arrayListOf<Entry>()

        list.forEachIndexed { index, listItem ->
            chartEntry.add(Entry(x=index.toFloat(), y=/*차트에 보여줄 데이터*/.toFloat()))
        }

        val chartDataSet = LineDataSet(chartData, "")

        binding.yourChartName.apply {
            data = LineData(chartDataSet)
            invalidate()
        }
        */


        }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }
    }
