package com.example.penjualan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvDaftar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Kalau sudah login sebelumnya, langsung ke Dashboard
        if (auth.currentUser != null) {
            goToDashboard()
            return
        }

        setContentView(R.layout.activity_login)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        etEmail    = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilEmail   = findViewById(R.id.tilEmail)
        tilPassword= findViewById(R.id.tilPassword)
        btnLogin   = findViewById(R.id.btnLogin)
        tvDaftar   = findViewById(R.id.tvDaftar)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Reset error state
            tilEmail.error    = null
            tilPassword.error = null

            // Validasi input
            var valid = true
            if (email.isEmpty()) {
                tilEmail.error = "Email tidak boleh kosong"
                valid = false
            }
            if (password.isEmpty()) {
                tilPassword.error = "Password tidak boleh kosong"
                valid = false
            }
            if (!valid) return@setOnClickListener

            setLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        goToDashboard()
                    } else {
                        val errorMsg = when (task.exception) {
                            is FirebaseAuthInvalidUserException ->
                                "Akun tidak ditemukan. Silakan daftar terlebih dahulu."
                            is FirebaseAuthInvalidCredentialsException ->
                                "Email atau password salah."
                            else ->
                                "Login gagal: ${task.exception?.message}"
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setLoading(loading: Boolean) {
        btnLogin.isEnabled = !loading
        btnLogin.text = if (loading) "Memproses..." else "Masuk 🐻"
    }

    private fun goToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
