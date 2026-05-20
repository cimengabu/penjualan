package com.example.inventoryapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.inventoryapp.databinding.ActivityProfilBinding
import com.example.inventoryapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserProfile()

        binding.btnEditProfil.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnGantiPassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val user = userDoc.toObject(User::class.java)

                binding.tvNama.text = user?.nama ?: "Pengguna"
                binding.tvEmail.text = auth.currentUser?.email ?: ""
                binding.tvRole.text = user?.role ?: "User"
                binding.tvMemberSince.text = user?.let {
                    java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id"))
                        .format(java.util.Date(it.createdAt))
                } ?: "-"
            } catch (e: Exception) {
                Toast.makeText(this@ProfilActivity, "Gagal load profil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profil, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditNama)
        etNama.setText(binding.tvNama.text)

        AlertDialog.Builder(this)
            .setTitle("Edit Profil")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val namaBaru = etNama.text.toString()
                if (namaBaru.isNotEmpty()) {
                    updateProfile(namaBaru)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateProfile(namaBaru: String) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                firestore.collection("users").document(userId)
                    .update("nama", namaBaru)
                    .await()

                binding.tvNama.text = namaBaru
                Toast.makeText(this@ProfilActivity, "Profil berhasil diupdate", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ProfilActivity, "Gagal update profil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ganti_password, null)
        val etPasswordLama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPasswordLama)
        val etPasswordBaru = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPasswordBaru)
        val etKonfirmasiPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etKonfirmasiPassword)

        AlertDialog.Builder(this)
            .setTitle("Ganti Password")
            .setView(dialogView)
            .setPositiveButton("Ganti") { _, _ ->
                val passwordLama = etPasswordLama.text.toString()
                val passwordBaru = etPasswordBaru.text.toString()
                val konfirmasi = etKonfirmasiPassword.text.toString()

                if (passwordBaru == konfirmasi && passwordBaru.length >= 6) {
                    changePassword(passwordLama, passwordBaru)
                } else {
                    Toast.makeText(this, "Password baru tidak cocok atau kurang dari 6 karakter", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser ?: return
        val credential = com.google.firebase.auth.EmailAuthProvider
            .getCredential(user.email ?: "", oldPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password berhasil diganti", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal mengganti password", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Password lama salah", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
    }
}