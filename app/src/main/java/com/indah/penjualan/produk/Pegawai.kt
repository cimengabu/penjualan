package com.example.inventoryapp.ui.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapp.adapters.PegawaiAdapter
import com.example.inventoryapp.databinding.ActivityPegawaiBinding
import com.example.inventoryapp.models.Pegawai
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPegawaiBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: PegawaiAdapter
    private val pegawaiList = mutableListOf<Pegawai>()
    private val cabangList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadCabang()
        loadPegawai()

        binding.btnTambahPegawai.setOnClickListener {
            showAddEmployeeDialog()
        }

        binding.btnRefresh.setOnClickListener {
            loadPegawai()
        }
    }

    private fun setupRecyclerView() {
        adapter = PegawaiAdapter(pegawaiList,
            onEditClick = { pegawai -> showEditEmployeeDialog(pegawai) },
            onDeleteClick = { pegawai -> deleteEmployee(pegawai) }
        )
        binding.rvPegawai.layoutManager = LinearLayoutManager(this)
        binding.rvPegawai.adapter = adapter
    }

    private fun loadCabang() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("cabang")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                cabangList.clear()
                cabangList.addAll(snapshot.documents.mapNotNull { it.getString("nama") })
                cabangList.add("Pusat")
            } catch (e: Exception) {
                cabangList.add("Pusat")
            }
        }
    }

    private fun loadPegawai() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("pegawai")
                    .whereEqualTo("userId", userId)
                    .orderBy("nama")
                    .get()
                    .await()

                pegawaiList.clear()
                pegawaiList.addAll(snapshot.toObjects(Pegawai::class.java))
                adapter.notifyDataSetChanged()

                binding.tvTotalPegawai.text = "Total Pegawai: ${pegawaiList.size}"
            } catch (e: Exception) {
                Toast.makeText(this@PegawaiActivity, "Gagal load pegawai", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddEmployeeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_pegawai, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaPegawai)
        val etPosisi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPosisi)
        val spinnerCabang = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCabang)
        val etTelepon = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTelepon)
        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEmail)

        // Setup cabang spinner
        val cabangAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cabangList)
        spinnerCabang.adapter = cabangAdapter

        AlertDialog.Builder(this)
            .setTitle("Tambah Pegawai Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString()
                val posisi = etPosisi.text.toString()
                val cabang = spinnerCabang.selectedItem.toString()
                val telepon = etTelepon.text.toString()
                val email = etEmail.text.toString()

                if (nama.isNotEmpty() && posisi.isNotEmpty()) {
                    saveEmployee(nama, posisi, cabang, telepon, email)
                } else {
                    Toast.makeText(this, "Nama dan posisi harus diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveEmployee(nama: String, posisi: String, cabang: String, telepon: String, email: String) {
        val userId = auth.currentUser?.uid ?: return
        val pegawai = Pegawai(
            id = firestore.collection("pegawai").document().id,
            userId = userId,
            nama = nama,
            posisi = posisi,
            cabang = cabang,
            telepon = telepon,
            email = email,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("pegawai").document(pegawai.id).set(pegawai)
            .addOnSuccessListener {
                Toast.makeText(this, "Pegawai berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                loadPegawai()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan pegawai", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditEmployeeDialog(pegawai: Pegawai) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_pegawai, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaPegawai)
        val etPosisi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPosisi)
        val etTelepon = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTelepon)
        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEmail)

        etNama.setText(pegawai.nama)
        etPosisi.setText(pegawai.posisi)
        etTelepon.setText(pegawai.telepon)
        etEmail.setText(pegawai.email)

        AlertDialog.Builder(this)
            .setTitle("Edit Pegawai")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val namaBaru = etNama.text.toString()
                val posisiBaru = etPosisi.text.toString()
                val teleponBaru = etTelepon.text.toString()
                val emailBaru = etEmail.text.toString()

                updateEmployee(pegawai.id, namaBaru, posisiBaru, teleponBaru, emailBaru)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateEmployee(id: String, nama: String, posisi: String, telepon: String, email: String) {
        val updates = mapOf(
            "nama" to nama,
            "posisi" to posisi,
            "telepon" to telepon,
            "email" to email
        )

        firestore.collection("pegawai").document(id).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Pegawai berhasil diupdate", Toast.LENGTH_SHORT).show()
                loadPegawai()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update pegawai", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteEmployee(pegawai: Pegawai) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pegawai")
            .setMessage("Apakah Anda yakin ingin menghapus ${pegawai.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                firestore.collection("pegawai").document(pegawai.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pegawai berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadPegawai()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus pegawai", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}