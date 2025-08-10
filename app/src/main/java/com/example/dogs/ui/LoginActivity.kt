package com.example.dogs.ui
// imports típicos:
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.dogs.data.TokenProvider
import com.example.dogs.databinding.ActivityLoginBinding
import com.example.dogs.net.NetworkEvents

class LoginActivity : AppCompatActivity() {
    private lateinit var vb: ActivityLoginBinding
    private lateinit var vm: LoginViewModel

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vm = ViewModelProvider(this)[LoginViewModel::class.java]

        // Permisos de notificación (opcional) y canal
        // NotificationUtils.createChannel(this)
        askNotificationPermissionIfNeeded()

        // Observa el estado del login (MVVM)
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

        // Observa request/response como textos (para que los veas en UI o Logcat)
        NetworkEvents.lastRequest.observe(this) { req ->
            android.util.Log.d("NET_REQ", req)
            // Si quieres, muestra en un TextView de debug:
            // vb.tvDebugRequest.text = req
        }
        NetworkEvents.lastResponse.observe(this) { res ->
            android.util.Log.d("NET_RES", res)
            // vb.tvDebugResponse.text = res
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