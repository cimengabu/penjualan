package com.example.penjualan.laporan

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelTransaksi
import com.example.penjualan.transaksi.TransaksiAdapter
import com.example.penjualan.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections

class LaporanActivity : AppCompatActivity() {

    private val transaksiRef = FirebaseUtils.getRef("transaksi")

    private lateinit var btnBack: ImageView
    private lateinit var rvLaporan: RecyclerView
    private lateinit var tvTotalPendapatanLaporan: TextView

    private lateinit var adapter: TransaksiAdapter
    private val transaksiList = ArrayList<ModelTransaksi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)

        btnBack = findViewById(R.id.btnBack)
        rvLaporan = findViewById(R.id.rvLaporan)
        tvTotalPendapatanLaporan = findViewById(R.id.tvTotalPendapatanLaporan)

        btnBack.setOnClickListener { finish() }

        adapter = TransaksiAdapter(transaksiList, onDeleteClick = { transaksi ->
            AlertDialog.Builder(this)
                .setTitle("Hapus Riwayat")
                .setMessage("Apakah Anda yakin ingin menghapus nota ${transaksi.nomorNota ?: "ini"}?")
                .setPositiveButton("Hapus") { _, _ ->
                    transaksi.idTransaksi?.let { id ->
                        transaksiRef.child(id).removeValue().addOnSuccessListener {
                            Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }, onPrintClick = { transaksi ->
            showPreviewDialog(transaksi)
        })
        
        rvLaporan.layoutManager = LinearLayoutManager(this)
        rvLaporan.adapter = adapter

        fetchTransaksi()
    }

    private fun fetchTransaksi() {
        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiList.clear()
                for (data in snapshot.children) {
                    val t = data.getValue(ModelTransaksi::class.java)
                    t?.let { transaksiList.add(it) }
                }
                adapter.updateData(transaksiList)
                updateTotalPendapatan()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LaporanActivity, "Gagal memuat laporan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTotalPendapatan() {
        val total = transaksiList.sumOf { it.totalHarga }
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvTotalPendapatanLaporan.text = "Total Pendapatan: ${fmt.format(total)}"
    }

    private fun showPreviewDialog(transaksi: ModelTransaksi) {
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        
        val header = "[C]<b><font size='big'>Nota Penjualan</font></b>\n" +
                     "[L]\n" +
                     "[L]No : ${transaksi.nomorNota ?: "-"}\n" +
                     "[L]Tgl: ${transaksi.tanggal ?: "-"} ${transaksi.waktu ?: "-"}\n" +
                     "[C]--------------------------------\n"
                     
        val itemsBuilder = StringBuilder()
        if (transaksi.items.isNotEmpty()) {
            transaksi.items.values.forEach { item ->
                itemsBuilder.append("[L]${item.namaProduk}\n")
                itemsBuilder.append("[L]  ${item.jumlah} x ${fmt.format(item.hargaSatuan)} [R]${fmt.format(item.subtotal)}\n")
            }
        } else {
            itemsBuilder.append("[L]${transaksi.namaProduk}\n")
            itemsBuilder.append("[L]  ${transaksi.jumlah} x ${fmt.format(transaksi.hargaSatuan)} [R]${fmt.format(transaksi.totalHarga)}\n")
        }
        
        val footer = "[C]--------------------------------\n" +
                     "[L]<b>Total</b> [R]<b>${fmt.format(transaksi.totalHarga)}</b>\n" +
                     "[L]Bayar    [R]${fmt.format(transaksi.jumlahBayar)}\n" +
                     "[L]Kembali  [R]${fmt.format(transaksi.kembalian)}\n" +
                     "[C]--------------------------------\n" +
                     "[C]Terima Kasih\n"

        val printText = header + itemsBuilder.toString() + footer
        
        // Buat preview teks polos (hilangkan tag formatting ESC/POS untuk ditampilkan ke layar)
        val previewLayar = printText.replace(Regex("\\[C\\]|\\[L\\]|\\[R\\]|<b>|</b>|<font.*?>|</font>|<u>|</u>"), "")

        AlertDialog.Builder(this)
            .setTitle("Pratinjau Nota (Preview)")
            .setMessage(previewLayar)
            .setPositiveButton("Cetak Sekarang") { _, _ ->
                doPrintSingle(printText)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private val PERMISSION_BLUETOOTH = 101
    private var pendingPrintText: String? = null

    private fun doPrintSingle(printText: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                pendingPrintText = printText
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), PERMISSION_BLUETOOTH)
                return
            }
        }
        executeBluetoothPrint(printText)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_BLUETOOTH && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pendingPrintText?.let { executeBluetoothPrint(it) }
        }
    }

    private fun executeBluetoothPrint(text: String) {
        try {
            val connections = BluetoothPrintersConnections.selectFirstPaired()
            if (connections != null) {
                val printer = EscPosPrinter(connections, 203, 48f, 32)
                printer.printFormattedText(text)
                printer.disconnectPrinter()
            } else {
                Toast.makeText(this, "Tidak ada printer Bluetooth yang dipasangkan!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
