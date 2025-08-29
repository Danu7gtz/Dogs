package com.example.dogs.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogs.data.TokenProvider

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tp = TokenProvider(applicationContext) // usa appContext por claridad
        val next = if (tp.isSessionValid()) AgendaActivity::class.java else LoginActivity::class.java

        startActivity(Intent(this, next).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }
}