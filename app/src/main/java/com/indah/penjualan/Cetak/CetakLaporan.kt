package com.example.inventoryapp.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.inventoryapp.databinding.ActivityCetakLaporanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CetakLaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCetakLaporanBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCetakLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnCetakLaporanStok.setOnClickListener {
            generateStockReport()
        }

        binding.btnCetakLaporanTransaksi.setOnClickListener {
            generateTransactionReport()
        }

        binding.btnCetakLaporanPegawai.setOnClickListener {
            generateEmployeeReport()
        }

        binding.btnShare.setOnClickListener {
            shareLastReport()
        }
    }

    private fun generateStockReport() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val produkSnapshot = firestore.collection("produk")
                    .whereEqualTo("userId", userId)
                    .orderBy("nama")
                    .get()
                    .await()

                val produkList = produkSnapshot.toObjects(com.example.inventoryapp.models.Produk::class.java)
                val htmlContent = generateStockReportHtml(produkList)
                saveAndOpenReport(htmlContent, "laporan_stok_${System.currentTimeMillis()}.html")

                Toast.makeText(this@CetakLaporanActivity, "Laporan stok berhasil dibuat", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@CetakLaporanActivity, "Gagal membuat laporan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateTransactionReport() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val transaksiSnapshot = firestore.collection("transaksi")
                    .whereEqualTo("userId", userId)
                    .orderBy("tanggal", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()

                val transaksiList = transaksiSnapshot.toObjects(com.example.inventoryapp.models.Transaksi::class.java)
                val totalPendapatan = transaksiList.sumOf { it.total }
                val htmlContent = generateTransactionReportHtml(transaksiList, totalPendapatan)
                saveAndOpenReport(htmlContent, "laporan_transaksi_${System.currentTimeMillis()}.html")

                Toast.makeText(this@CetakLaporanActivity, "Laporan transaksi berhasil dibuat", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@CetakLaporanActivity, "Gagal membuat laporan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateEmployeeReport() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val pegawaiSnapshot = firestore.collection("pegawai")
                    .whereEqualTo("userId", userId)
                    .orderBy("nama")
                    .get()
                    .await()

                val pegawaiList = pegawaiSnapshot.toObjects(com.example.inventoryapp.models.Pegawai::class.java)
                val htmlContent = generateEmployeeReportHtml(pegawaiList)
                saveAndOpenReport(htmlContent, "laporan_pegawai_${System.currentTimeMillis()}.html")

                Toast.makeText(this@CetakLaporanActivity, "Laporan pegawai berhasil dibuat", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@CetakLaporanActivity, "Gagal membuat laporan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateStockReportHtml(produkList: List<com.example.inventoryapp.models.Produk>): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val date = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale("id")).format(Date())

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Laporan Stok Barang</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #4CAF50; text-align: center; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                    th { background-color: #4CAF50; color: white; }
                    tr:nth-child(even) { background-color: #f2f2f2; }
                    .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #666; }
                    .total { font-weight: bold; margin-top: 20px; }
                </style>
            </head>
            <body>
                <h1>LAPORAN STOK BARANG</h1>
                <p style="text-align: center; color: #666;">Dicetak pada: $date</p>
                
                <table>
                    <thead>
                        <tr>
                            <th>No</th>
                            <th>Nama Produk</th>
                            <th>Kategori</th>
                            <th>Stok</th>
                            <th>Harga Beli</th>
                            <th>Harga Jual</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${produkList.mapIndexed { index, produk ->
            """
                            <tr>
                                <td>${index + 1}</td>
                                <td>${produk.nama}</td>
                                <td>${produk.kategori}</td>
                                <td>${produk.stok}</td>
                                <td>${format.format(produk.hargaBeli)}</td>
                                <td>${format.format(produk.hargaJual)}</td>
                            </tr>
                            """
        }.joinToString("")}
                    </tbody>
                </table>
                
                <div class="total">
                    <p><strong>Total Jenis Produk:</strong> ${produkList.size}</p>
                    <p><strong>Total Stok Keseluruhan:</strong> ${produkList.sumOf { it.stok }}</p>
                    <p><strong>Total Nilai Stok:</strong> ${format.format(produkList.sumOf { it.stok * it.hargaJual })}</p>
                </div>
                
                <div class="footer">
                    <p>Laporan ini dibuat secara otomatis oleh sistem InventoryApp</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun generateTransactionReportHtml(transaksiList: List<com.example.inventoryapp.models.Transaksi>, totalPendapatan: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val date = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale("id")).format(Date())

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Laporan Transaksi</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #2196F3; text-align: center; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                    th { background-color: #2196F3; color: white; }
                    tr:nth-child(even) { background-color: #f2f2f2; }
                    .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #666; }
                    .total { font-weight: bold; margin-top: 20px; font-size: 18px; color: #2196F3; }
                </style>
            </head>
            <body>
                <h1>LAPORAN TRANSAKSI</h1>
                <p style="text-align: center; color: #666;">Dicetak pada: $date</p>
                
                <table>
                    <thead>
                        <tr>
                            <th>No</th>
                            <th>Tanggal</th>
                            <th>Produk</th>
                            <th>Jumlah</th>
                            <th>Harga Satuan</th>
                            <th>Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${transaksiList.mapIndexed { index, transaksi ->
            """
                            <tr>
                                <td>${index + 1}</td>
                                <td>${transaksi.tanggal}</td>
                                <td>${transaksi.namaProduk}</td>
                                <td>${transaksi.jumlah}</td>
                                <td>${format.format(transaksi.hargaSatuan)}</td>
                                <td>${format.format(transaksi.total)}</td>
                            </tr>
                            """
        }.joinToString("")}
                    </tbody>
                </table>
                
                <div class="total">
                    <p><strong>Total Pendapatan:</strong> ${format.format(totalPendapatan)}</p>
                    <p><strong>Jumlah Transaksi:</strong> ${transaksiList.size}</p>
                </div>
                
                <div class="footer">
                    <p>Laporan ini dibuat secara otomatis oleh sistem InventoryApp</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun generateEmployeeReportHtml(pegawaiList: List<com.example.inventoryapp.models.Pegawai>): String {
        val date = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale("id")).format(Date())

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Laporan Pegawai</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #FF9800; text-align: center; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                    th { background-color: #FF9800; color: white; }
                    tr:nth-child(even) { background-color: #f2f2f2; }
                    .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <h1>LAPORAN DATA PEGAWAI</h1>
                <p style="text-align: center; color: #666;">Dicetak pada: $date</p>
                
                <table>
                    <thead>
                        <tr>
                            <th>No</th>
                            <th>Nama Pegawai</th>
                            <th>Posisi</th>
                            <th>Cabang</th>
                            <th>Telepon</th>
                            <th>Email</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${pegawaiList.mapIndexed { index, pegawai ->
            """
                            <tr>
                                <td>${index + 1}</td>
                                <td>${pegawai.nama}</td>
                                <td>${pegawai.posisi}</td>
                                <td>${pegawai.cabang}</td>
                                <td>${pegawai.telepon}</td>
                                <td>${pegawai.email}</td>
                            </tr>
                            """
        }.joinToString("")}
                    </tbody>
                </table>
                
                <div class="footer">
                    <p><strong>Total Pegawai:</strong> ${pegawaiList.size}</p>
                    <p>Laporan ini dibuat secara otomatis oleh sistem InventoryApp</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun saveAndOpenReport(htmlContent: String, fileName: String) {
        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(htmlContent.toByteArray())
            }

            // Save reference to last report
            getSharedPreferences("report_prefs", MODE_PRIVATE)
                .edit()
                .putString("last_report_path", file.absolutePath)
                .apply()

            // Open with browser
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/html")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Buka Laporan"))

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal menyimpan laporan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareLastReport() {
        val lastReportPath = getSharedPreferences("report_prefs", MODE_PRIVATE)
            .getString("last_report_path", null)

        if (lastReportPath != null) {
            val file = File(lastReportPath)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/html"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan"))
            } else {
                Toast.makeText(this, "Tidak ada laporan terakhir", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Belum ada laporan yang dibuat", Toast.LENGTH_SHORT).show()
        }
    }
}