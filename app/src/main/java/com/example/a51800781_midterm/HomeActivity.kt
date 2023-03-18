package com.example.a51800781_midterm

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class HomeActivity : AppCompatActivity() {
    private lateinit var btn: Button
    private lateinit var startInput: EditText
    private lateinit var endInput: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn = findViewById(R.id.button)
        startInput = findViewById(R.id.startInput)
        endInput = findViewById(R.id.endInput)
        btn.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("start", startInput.text.toString())
            intent.putExtra("end", endInput.text.toString())
            startActivity(intent)
        }
    }
}