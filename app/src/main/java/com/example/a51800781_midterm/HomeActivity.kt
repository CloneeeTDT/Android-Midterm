package com.example.a51800781_midterm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var btn: Button
    private lateinit var tdtuBtn: Button
    private lateinit var startInput: EditText
    private lateinit var endInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var locationBtn: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn = findViewById(R.id.button)
        tdtuBtn = findViewById(R.id.findTDTBtn)
        startInput = findViewById(R.id.startInput)
        endInput = findViewById(R.id.endInput)
        locationInput = findViewById(R.id.editTextLocation)
        locationBtn = findViewById(R.id.locationBtn)
        btn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            val start: String = startInput.text.toString()
            val end: String = endInput.text.toString()
            intent.putExtra("start", start)
            intent.putExtra("end", end)
            intent.putExtra("key", "AIzaSyCOi9tAVIFl2RCoAcaur5_9iIaw50tdpOI")
            intent.putExtra("mode", "FIND_PATH")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        tdtuBtn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("mode", "FIND_TDTU")
            startActivity(intent)
        }
        locationBtn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("mode", "FIND_LOCATION")
            intent.putExtra("location", locationInput.text)
            startActivity(intent)
        }
    }
}