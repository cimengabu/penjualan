package com.example.penjualan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnRegister: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etEmail    = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilEmail   = findViewById(R.id.tilEmail)
        tilPassword= findViewById(R.id.tilPassword)
        btnRegister= findViewById(R.id.btnRegister)

        val tvLogin = findViewById<android.widget.TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Reset error
            tilEmail.error    = null
            tilPassword.error = null

            // Validasi
            var valid = true
            if (email.isEmpty()) {
                tilEmail.error = "Email tidak boleh kosong"
                valid = false
            }
            if (password.isEmpty()) {
                tilPassword.error = "Password tidak boleh kosong"
                valid = false
            } else if (password.length < 6) {
                tilPassword.error = "Password minimal 6 karakter"
                valid = false
            }
            if (!valid) return@setOnClickListener

            setLoading(true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "🐻 Akun berhasil dibuat! Silakan masuk.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Kembali ke LoginActivity
                    } else {
                        val errorMsg = when (task.exception) {
                            is FirebaseAuthUserCollisionException ->
                                "Email ini sudah terdaftar. Coba masuk."
                            is FirebaseAuthWeakPasswordException ->
                                "Password terlalu lemah. Gunakan minimal 6 karakter."
                            else ->
                                "Pendaftaran gagal: ${task.exception?.message}"
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLogin.setOnClickListener { finish() }
    }

    private fun setLoading(loading: Boolean) {
        btnRegister.isEnabled = !loading
        btnRegister.text = if (loading) "Mendaftarkan..." else "Daftar Sekarang 🐻"
    }
}
