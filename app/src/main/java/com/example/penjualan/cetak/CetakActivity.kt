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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    private fun doPrint(transaksiList: List<ModelTransaksi>) {
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                createWebPrintJob(view)
                myWebView = null
            }
        }

        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val dateString = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale("id", "ID")).format(Date())
        
        var totalKeseluruhan = 0.0
        val tableRows = StringBuilder()

        if (transaksiList.isEmpty()) {
            tableRows.append("<tr><td colspan='4' style='text-align:center;'>Belum ada transaksi</td></tr>")
        } else {
            for (t in transaksiList) {
                totalKeseluruhan += t.totalHarga
                tableRows.append("""
                    <tr>
                        <td>${t.namaProduk ?: "-"}</td>
                        <td>${t.kategori ?: "-"}</td>
                        <td style='text-align:center;'>${t.jumlah}</td>
                        <td style='text-align:right;'>${fmt.format(t.totalHarga)}</td>
                    </tr>
                """.trimIndent())
            }
        }

        val htmlDocument = """
            <html>
                <head>
                    <style>
                        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 40px; color: #333; }
                        .header { text-align: center; margin-bottom: 30px; }
                        h1 { color: #4F46E5; margin-bottom: 5px; font-size: 28px; }
                        .subtitle { color: #64748B; font-size: 14px; margin-top: 0; }
                        .meta-info { margin-bottom: 20px; font-size: 14px; }
                        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                        th, td { border-bottom: 1px solid #E2E8F0; padding: 12px 8px; text-align: left; }
                        th { background-color: #F8FAFC; color: #475569; font-weight: bold; text-transform: uppercase; font-size: 12px; }
                        td { font-size: 14px; }
                        .total-row td { font-weight: bold; font-size: 16px; color: #0F766E; border-top: 2px solid #4F46E5; }
                        .footer { text-align: center; margin-top: 60px; font-size: 12px; color: #94A3B8; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>LAPORAN PENJUALAN</h1>
                        <p class="subtitle">Laporan Resmi Transaksi Toko</p>
                    </div>
                    <div class="meta-info">
                        <p><b>Tanggal Cetak:</b> $dateString</p>
                    </div>
                    <table>
                        <tr>
                            <th>Nama Produk</th>
                            <th>Kategori</th>
                            <th style='text-align:center;'>Qty</th>
                            <th style='text-align:right;'>Total Harga</th>
                        </tr>
                        $tableRows
                        <tr class="total-row">
                            <td colspan="3" style='text-align:right;'>TOTAL PENDAPATAN</td>
                            <td style='text-align:right;'>${fmt.format(totalKeseluruhan)}</td>
                        </tr>
                    </table>
                    <div class="footer">
                        <p>Dokumen ini di-generate secara otomatis oleh sistem.</p>
                        <p>&copy; ${Calendar.getInstance().get(Calendar.YEAR)} Aplikasi Penjualan</p>
                    </div>
                </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
        myWebView = webView
    }

    private fun createWebPrintJob(webView: WebView) {
        val printManager = this.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter("Laporan Penjualan")
        val jobName = "${getString(R.string.app_name)} Document"

        try {
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
