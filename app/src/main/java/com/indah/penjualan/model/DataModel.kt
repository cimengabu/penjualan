// Produk.kt
package com.example.inventoryapp.models

data class Produk(
    val id: String = "",
    val userId: String = "",
    val nama: String = "",
    val kategori: String = "",
    val stok: Int = 0,
    val hargaBeli: Double = 0.0,
    val hargaJual: Double = 0.0,
    val timestamp: Long = 0L
)

// Kategori.kt
package com.example.inventoryapp.models

data class Kategori(
    val id: String = "",
    val userId: String = "",
    val nama: String = "",
    val deskripsi: String = "",
    val timestamp: Long = 0L,
    var jumlahProduk: Int = 0
)

// Transaksi.kt
package com.example.inventoryapp.models

data class Transaksi(
    val id: String = "",
    val userId: String = "",
    val produkId: String = "",
    val namaProduk: String = "",
    val jumlah: Int = 0,
    val hargaSatuan: Double = 0.0,
    val total: Double = 0.0,
    val tanggal: String = "",
    val timestamp: Long = 0L
)