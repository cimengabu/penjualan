package com.example.penjualan.pos

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import androidx.print.PrintHelper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.penjualan.FirebaseUtils
import com.example.penjualan.LocaleHelper
import com.example.penjualan.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections

class ReceiptActivity : AppCompatActivity() {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private val profilRef = FirebaseUtils.getRef("profil")

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        val nomorNota   = intent.getStringExtra("NOMOR_NOTA") ?: "-"
        val tanggal     = intent.getStringExtra("TANGGAL") ?: "-"
        val waktu       = intent.getStringExtra("WAKTU") ?: "-"
        val kasir       = intent.getStringExtra("NAMA_KASIR") ?: "-"
        val pelanggan   = intent.getStringExtra("NAMA_PELANGGAN") ?: "Umum"
        val noHp        = intent.getStringExtra("NO_HP_PELANGGAN") ?: ""
        val metode      = intent.getStringExtra("METODE") ?: "Tunai"
        val subtotal    = intent.getDoubleExtra("SUBTOTAL", 0.0)
        val diskon      = intent.getDoubleExtra("DISKON", 0.0)
        val pajak       = intent.getDoubleExtra("PAJAK", 0.0)
        val total       = intent.getDoubleExtra("TOTAL", 0.0)
        val dibayar     = intent.getDoubleExtra("DIBAYAR", 0.0)
        val kembalian   = intent.getDoubleExtra("KEMBALIAN", 0.0)
        val items       = intent.getParcelableArrayListExtra<CheckoutActivity.CartItemParcel>("ITEMS") ?: emptyList<CheckoutActivity.CartItemParcel>()

        // Bind views
        findViewById<TextView>(R.id.tvNomorNota).text = nomorNota
        findViewById<TextView>(R.id.tvNotaInvoice).text = nomorNota
        findViewById<TextView>(R.id.tvTanggalNota).text = "$tanggal  $waktu"
        findViewById<TextView>(R.id.tvWaktuNota).text = waktu
        findViewById<TextView>(R.id.tvKasirNota).text = kasir
        val pelangganText = if (noHp.isNotBlank()) "$pelanggan  ($noHp)" else pelanggan
        findViewById<TextView>(R.id.tvPelangganNota).text = pelangganText
        findViewById<TextView>(R.id.tvSubtotalNota).text = fmt.format(subtotal)
        findViewById<TextView>(R.id.tvDiskonNota).text = "- ${fmt.format(diskon)}"
        findViewById<TextView>(R.id.tvPajakNota).text = fmt.format(pajak)
        findViewById<TextView>(R.id.tvTotalNota).text = fmt.format(total)
        findViewById<TextView>(R.id.tvMetodeNota).text = metode
        findViewById<TextView>(R.id.tvDibayarNota).text = fmt.format(dibayar)
        findViewById<TextView>(R.id.tvKembalianNota).text = fmt.format(kembalian)

        // Populate item rows
        val llItems = findViewById<LinearLayout>(R.id.llItemsNota)
        items.forEachIndexed { index, item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 4, 0, 4)
            }
            // Item name row
            val nameRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            nameRow.addView(TextView(this).apply {
                text = "${index + 1}. ${item.namaProduk}"
                textSize = 11f
                setTextColor(0xFF212121.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f)
            })
            nameRow.addView(TextView(this).apply {
                text = "×${item.jumlah}"
                textSize = 11f
                gravity = android.view.Gravity.CENTER
                setTextColor(0xFF757575.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            nameRow.addView(TextView(this).apply {
                text = fmt.format(item.hargaSatuan)
                textSize = 11f
                gravity = android.view.Gravity.END
                setTextColor(0xFF757575.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            })
            nameRow.addView(TextView(this).apply {
                text = fmt.format(item.subtotal)
                textSize = 11f
                gravity = android.view.Gravity.END
                setTextColor(0xFF212121.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            })
            row.addView(nameRow)

            // Kategori sub-label
            row.addView(TextView(this).apply {
                text = "   ${item.kategori}"
                textSize = 10f
                setTextColor(0xFF9E9E9E.toInt())
            })

            llItems.addView(row)
        }

        // Load store profile for header
        profilRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alamat = snapshot.child("alamatToko").getValue(String::class.java) ?: "Jl. -"
                val telp = snapshot.child("noTeleponToko").getValue(String::class.java) ?: "-"
                runOnUiThread {
                    try {
                        findViewById<TextView>(R.id.tvAlamatToko).text = alamat
                        findViewById<TextView>(R.id.tvTelpToko).text = "Telp: $telp"
                    } catch (_: Exception) {}
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Cabang
        FirebaseUtils.getRef("cabang").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cabangData = snapshot.children.firstOrNull()
                val namaCabang = cabangData?.child("namaCabang")?.getValue(String::class.java) ?: "-"
                runOnUiThread {
                    try {
                        findViewById<TextView>(R.id.tvCabangNota).text = "Cabang: $namaCabang"
                    } catch (_: Exception) {}
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Tombol Bagikan
        findViewById<MaterialButton>(R.id.btnShareNota).setOnClickListener {
            shareReceiptAsImage()
        }

        // Tombol Cetak
        findViewById<MaterialButton>(R.id.btnPrintNota).setOnClickListener {
            printReceipt()
        }

        // Tombol Transaksi Baru
        findViewById<MaterialButton>(R.id.btnTransaksiBaruNota).setOnClickListener {
            val intent = Intent(this, PosActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun shareReceiptAsImage() {
        try {
            val cardNota = findViewById<View>(R.id.cardNota)
            val bmp = Bitmap.createBitmap(cardNota.width, cardNota.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(android.graphics.Color.WHITE)
            cardNota.draw(canvas)

            val file = File(cacheDir, "nota_lumina.png")
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }

            val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Nota Pembayaran - Lumina POS")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.bagikan)))
        } catch (e: Exception) {
            Toast.makeText(this, "${getString(R.string.gagal_membagikan_nota)} ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val PERMISSION_BLUETOOTH = 101

    private fun printReceipt() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), PERMISSION_BLUETOOTH)
                return
            }
        }
        doBluetoothPrint()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_BLUETOOTH && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doBluetoothPrint()
        }
    }

    private fun doBluetoothPrint() {
        val header = "[C]<b><font size='big'>Nota Penjualan</font></b>\n" +
                     "[C]${findViewById<TextView>(R.id.tvAlamatToko).text}\n" +
                     "[C]${findViewById<TextView>(R.id.tvTelpToko).text}\n" +
                     "[L]\n" +
                     "[L]No : ${findViewById<TextView>(R.id.tvNomorNota).text}\n" +
                     "[L]Tgl: ${findViewById<TextView>(R.id.tvTanggalNota).text}\n" +
                     "[C]--------------------------------\n"
                     
        val itemsBuilder = StringBuilder()
        val items = intent.getParcelableArrayListExtra<CheckoutActivity.CartItemParcel>("ITEMS") ?: emptyList()
        items.forEach { item ->
            itemsBuilder.append("[L]${item.namaProduk}\n")
            itemsBuilder.append("[L]  ${item.jumlah} x ${fmt.format(item.hargaSatuan)} [R]${fmt.format(item.subtotal)}\n")
        }
        
        val footer = "[C]--------------------------------\n" +
                     "[L]Subtotal [R]${findViewById<TextView>(R.id.tvSubtotalNota).text}\n" +
                     "[L]Diskon   [R]${findViewById<TextView>(R.id.tvDiskonNota).text}\n" +
                     "[L]Pajak    [R]${findViewById<TextView>(R.id.tvPajakNota).text}\n" +
                     "[L]<b>Total</b> [R]<b>${findViewById<TextView>(R.id.tvTotalNota).text}</b>\n" +
                     "[L]Bayar    [R]${findViewById<TextView>(R.id.tvDibayarNota).text}\n" +
                     "[L]Kembali  [R]${findViewById<TextView>(R.id.tvKembalianNota).text}\n" +
                     "[C]--------------------------------\n" +
                     "[C]Terima Kasih\n"

        val printText = header + itemsBuilder.toString() + footer
        
        val previewLayar = printText.replace(Regex("\\[C\\]|\\[L\\]|\\[R\\]|<b>|</b>|<font.*?>|</font>|<u>|</u>"), "")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Pratinjau Nota (Preview)")
            .setMessage(previewLayar)
            .setPositiveButton("Cetak Sekarang") { _, _ ->
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
