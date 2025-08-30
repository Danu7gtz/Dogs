package com.example.dogs.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.dogs.R
import com.example.dogs.data.TokenProvider
import com.example.dogs.databinding.ActivityLoginBinding
import com.example.dogs.net.NetworkEvents
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.math.max

class LoginActivity : AppCompatActivity() {

    private lateinit var vb: ActivityLoginBinding
    private lateinit var vm: LoginViewModel

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- FCM token (opcional)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM", "Token: $token")
                println("Tu token de FCM es: $token")
            }

        // --- Ajustes de ventana: clave para que el teclado NO tape la card
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // --- ViewBinding (una sola vez)
        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        // --- Insets del teclado: añade paddingBottom dinámico al scroll
        ViewCompat.setOnApplyWindowInsetsListener(vb.scrollLogin) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val base = v.paddingBottom
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, max(base, ime.bottom))
            insets
        }

        // --- Auto-scroll al enfocar campos
        vb.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) vb.scrollLogin.post {
                vb.scrollLogin.smoothScrollTo(0, vb.etEmail.top)
            }
        }
        vb.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) vb.scrollLogin.post {
                vb.scrollLogin.smoothScrollTo(0, vb.etPassword.top)
            }
        }

        // --- ViewModel
        vm = ViewModelProvider(this)[LoginViewModel::class.java]

        // --- Permiso de notificaciones (Android 13+)
        askNotificationPermissionIfNeeded()

        // --- Observers de estado de login
        vm.state.observe(this) { st ->
            when (st) {
                is LoginState.Idle -> { /* nada */ }
                is LoginState.Loading -> {
                    Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
                }
                is LoginState.Success -> {
                    // Guarda el token y calcula la expiración a 24h desde ahora
                    val tp = TokenProvider(this)
                    tp.saveToken(st.data.token) // <- este método ya se encarga de guardar token + expiración

                    Toast.makeText(this, "Login OK", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, AgendaActivity::class.java))
                    finish()
                }
                is LoginState.Error -> {
                    Toast.makeText(this, "Error: ${st.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        // --- Logs de red (opcionales)
        NetworkEvents.lastRequest.observe(this) { req ->
            Log.d("NET_REQ", req)
        }
        NetworkEvents.lastResponse.observe(this) { res ->
            Log.d("NET_RES", res)
        }

        // --- Acción del botón Login
        vb.btnLogin.setOnClickListener {
            val email = vb.etEmail.text.toString().trim()
            val pass  = vb.etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
            } else {
                vm.login(email, pass)
            }
        }
    }

    private fun askNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}