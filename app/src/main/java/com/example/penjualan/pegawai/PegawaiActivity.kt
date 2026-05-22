package com.example.penjualan.pegawai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelPegawai
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PegawaiActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://penjualan-indah-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val pegawaiRef = database.getReference("pegawai")

    private lateinit var btnBack: ImageView
    private lateinit var rvPegawai: RecyclerView
    private lateinit var fabTambahPegawai: FloatingActionButton
    private lateinit var searchView: SearchView

    private lateinit var adapter: PegawaiAdapter
    private val pegawaiList = ArrayList<ModelPegawai>()
    private val originalList = ArrayList<ModelPegawai>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pegawai)

        btnBack = findViewById(R.id.btnBack)
        rvPegawai = findViewById(R.id.rvPegawai)
        fabTambahPegawai = findViewById(R.id.fabTambahPegawai)
        searchView = findViewById(R.id.searchView)

        btnBack.setOnClickListener { finish() }

        adapter = PegawaiAdapter(
            pegawaiList,
            onEditClick = { showAddOrEditDialog(it) },
            onDeleteClick = { showDeleteDialog(it) }
        )
        rvPegawai.layoutManager = LinearLayoutManager(this)
        rvPegawai.adapter = adapter

        fetchPegawai()

        fabTambahPegawai.setOnClickListener { showAddOrEditDialog(null) }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { filter(query); return true }
            override fun onQueryTextChange(newText: String?): Boolean { filter(newText); return true }
        })
    }

    private fun fetchPegawai() {
        pegawaiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pegawaiList.clear()
                originalList.clear()
                for (data in snapshot.children) {
                    val p = data.getValue(ModelPegawai::class.java)
                    p?.let { pegawaiList.add(it); originalList.add(it) }
                }
                adapter.updateData(pegawaiList)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PegawaiActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String?) {
        val filtered = if (query.isNullOrBlank()) originalList
        else originalList.filter { it.namaPegawai?.contains(query, ignoreCase = true) == true }
        adapter.updateData(ArrayList(filtered))
    }

    private fun showAddOrEditDialog(pegawai: ModelPegawai?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pegawai, null)
        val etNama = dialogView.findViewById<TextInputEditText>(R.id.etNamaPegawai)
        val etJabatan = dialogView.findViewById<TextInputEditText>(R.id.etJabatan)
        val etNoHp = dialogView.findViewById<TextInputEditText>(R.id.etNoHp)
        val actStatus = dialogView.findViewById<AutoCompleteTextView>(R.id.actStatusPegawai)

        val statusOptions = arrayOf("Aktif", "Nonaktif")
        actStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusOptions))

        if (pegawai != null) {
            etNama.setText(pegawai.namaPegawai)
            etJabatan.setText(pegawai.jabatan)
            etNoHp.setText(pegawai.noHp)
            actStatus.setText(pegawai.statusPegawai, false)
        } else {
            actStatus.setText(statusOptions[0], false)
        }

        AlertDialog.Builder(this)
            .setTitle(if (pegawai != null) "Ubah Pegawai" else "Tambah Pegawai")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama pegawai wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val key = pegawai?.idPegawai ?: pegawaiRef.push().key ?: return@setPositiveButton
                val data = ModelPegawai(
                    idPegawai = key,
                    namaPegawai = nama,
                    jabatan = etJabatan.text.toString().trim(),
                    noHp = etNoHp.text.toString().trim(),
                    statusPegawai = actStatus.text.toString()
                )
                pegawaiRef.child(key).setValue(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, if (pegawai != null) "Pegawai diperbarui" else "Pegawai ditambahkan", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteDialog(pegawai: ModelPegawai) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pegawai")
            .setMessage("Hapus pegawai \"${pegawai.namaPegawai}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                val id = pegawai.idPegawai ?: return@setPositiveButton
                pegawaiRef.child(id).removeValue()
                    .addOnSuccessListener { Toast.makeText(this, "Pegawai dihapus", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
