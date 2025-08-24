package com.example.dogs.ui
// imports típicos:
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.dogs.data.TokenProvider
import com.example.dogs.databinding.ActivityLoginBinding
import com.example.dogs.net.NetworkEvents
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    private lateinit var vb: ActivityLoginBinding
    private lateinit var vm: LoginViewModel

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM", "Token: $token")
                // Opcional: mostrarlo en pantalla para copiar
                println("Tu token de FCM es: $token")
            }


        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        ViewCompat.setOnApplyWindowInsetsListener(vb.scrollLogin) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val base =  v.paddingBottom
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, maxOf(base, ime.bottom))
            insets
        }

        vb.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) vb.scrollLogin.post { vb.scrollLogin.smoothScrollTo(0, vb.etEmail.top) }
        }
        vb.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) vb.scrollLogin.post { vb.scrollLogin.smoothScrollTo(0, vb.etPassword.top) }
        }


        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vm = ViewModelProvider(this)[LoginViewModel::class.java]

        askNotificationPermissionIfNeeded()

        vm.state.observe(this) { st ->
            when (st) {
                is LoginState.Idle -> { /* UI idle */ }
                is LoginState.Loading -> {
                    Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
                }
                is LoginState.Success -> {
                    // guarda token y navega
                    TokenProvider(this).saveToken(st.data.token)
                    Toast.makeText(this, "Login OK", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AgendaActivity::class.java))
                    finish()
                }
                is LoginState.Error -> {
                    Toast.makeText(this, "Error: ${st.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        NetworkEvents.lastRequest.observe(this) { req ->
            android.util.Log.d("NET_REQ", req)
        }
        NetworkEvents.lastResponse.observe(this) { res ->
            android.util.Log.d("NET_RES", res)
        }

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