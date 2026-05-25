package com.example.penjualan

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnRegister: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etName     = findViewById(R.id.etName)
        etEmail    = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilName    = findViewById(R.id.tilName)
        tilEmail   = findViewById(R.id.tilEmail)
        tilPassword= findViewById(R.id.tilPassword)
        btnRegister= findViewById(R.id.btnRegister)

        val tvLogin = findViewById<android.widget.TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val name     = etName.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Reset error
            tilName.error     = null
            tilEmail.error    = null
            tilPassword.error = null

            // Validasi
            var valid = true
            if (name.isEmpty()) {
                tilName.error = "Nama tidak boleh kosong"
                valid = false
            }
            if (email.isEmpty()) {
                tilEmail.error = "Email tidak boleh kosong"
                valid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Format email tidak valid"
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
                        Log.d("RegisterActivity", "Register sukses")
                        val prefs = getSharedPreferences("PenjualanPrefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putString("USER_NAME", name).apply()
                        Toast.makeText(
                            this,
                            "Akun berhasil dibuat! Silakan masuk.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // Kembali ke LoginActivity
                    } else {
                        val exception = task.exception
                        Log.e("RegisterActivity", "Register gagal", exception)
                        val errorMsg = when (exception) {
                            is FirebaseAuthUserCollisionException ->
                                "Email ini sudah terdaftar. Coba masuk."
                            is FirebaseAuthWeakPasswordException ->
                                "Password terlalu lemah. Gunakan minimal 6 karakter."
                            is FirebaseNetworkException ->
                                "Tidak ada koneksi internet. Coba lagi."
                            else ->
                                "Pendaftaran gagal: ${exception?.localizedMessage}"
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLogin.setOnClickListener { finish() }
    }

    private fun setLoading(loading: Boolean) {
        btnRegister.isEnabled = !loading
        btnRegister.text = if (loading) "Mendaftarkan..." else "Daftar Sekarang"
    }
}
