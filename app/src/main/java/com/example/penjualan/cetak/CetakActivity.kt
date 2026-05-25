package com.example.penjualan.cetak

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.penjualan.R
import com.example.penjualan.model.ModelTransaksi
import com.example.penjualan.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections

class CetakActivity : AppCompatActivity() {

    private var myWebView: WebView? = null

    private val transaksiRef = FirebaseUtils.getRef("transaksi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cetak)

        val btnBack: ImageView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val btnCetakSekarang: Button = findViewById(R.id.btnCetakSekarang)
        btnCetakSekarang.setOnClickListener {
            fetchDataAndPrint()
        }
    }

    private fun fetchDataAndPrint() {
        transaksiRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transaksiList = ArrayList<ModelTransaksi>()
                for (data in snapshot.children) {
                    val t = data.getValue(ModelTransaksi::class.java)
                    if (t != null) transaksiList.add(t)
                }
                doPrint(transaksiList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CetakActivity, "Gagal mengambil data transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private val PERMISSION_BLUETOOTH = 101
    private var pendingTransaksiList: List<ModelTransaksi>? = null

    private fun doPrint(transaksiList: List<ModelTransaksi>) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                pendingTransaksiList = transaksiList
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), PERMISSION_BLUETOOTH)
                return
            }
        }
        doBluetoothPrint(transaksiList)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_BLUETOOTH && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pendingTransaksiList?.let { doBluetoothPrint(it) }
        }
    }

    private fun doBluetoothPrint(transaksiList: List<ModelTransaksi>) {
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val dateString = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        
        val header = "[C]<b><font size='big'>Laporan Penjualan</font></b>\n" +
                     "[C]Tanggal: $dateString\n" +
                     "[C]--------------------------------\n"
                     
        var totalKeseluruhan = 0.0
        val itemsBuilder = StringBuilder()
        
        if (transaksiList.isEmpty()) {
            itemsBuilder.append("[C]Belum ada transaksi\n")
        } else {
            for (t in transaksiList) {
                totalKeseluruhan += t.totalHarga
                itemsBuilder.append("[L]${t.namaProduk ?: "-"}\n")
                itemsBuilder.append("[L]  Qty: ${t.jumlah} [R]${fmt.format(t.totalHarga)}\n")
            }
        }
        
        val footer = "[C]--------------------------------\n" +
                     "[L]<b>Total Pendapatan</b> [R]<b>${fmt.format(totalKeseluruhan)}</b>\n"
        
        val printText = header + itemsBuilder.toString() + footer
        val previewLayar = printText.replace(Regex("\\[C\\]|\\[L\\]|\\[R\\]|<b>|</b>|<font.*?>|</font>|<u>|</u>"), "")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Pratinjau Cetak Laporan")
            .setMessage(previewLayar)
            .setPositiveButton("Cetak Semua Laporan") { _, _ ->
                executePrint(printText)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun executePrint(printText: String) {
        try {
            val connections = BluetoothPrintersConnections.selectFirstPaired()
            if (connections != null) {
                val printer = EscPosPrinter(connections, 203, 48f, 32)
                printer.printFormattedText(printText)
                printer.disconnectPrinter()
            } else {
                Toast.makeText(this, "Tidak ada printer Bluetooth yang dipasangkan!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mencetak Bluetooth: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}