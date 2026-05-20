// User.kt
package com.example.inventoryapp.models

data class User(
    val id: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "user",
    val createdAt: Long = System.currentTimeMillis()
)

// Stok.kt
package com.example.inventoryapp.models

data class Stok(
    val id: String = "",
    val userId: String = "",
    val namaBarang: String = "",
    val kategori: String = "",
    val jumlah: Int = 0,
    val hargaBeli: Double = 0.0,
    val hargaJual: Double = 0.0,
    val timestamp: Long = 0L
)

// Pemesanan.kt
package com.example.inventoryapp.models

data class Pemesanan(
    val id: String = "",
    val userId: String = "",
    val barangId: String = "",
    val namaBarang: String = "",
    val jumlah: Int = 0,
    val totalHarga: Double = 0.0,
    val status: String = "pending",
    val tanggal: Long = System.currentTimeMillis()
)

// Cabang.kt
package com.example.inventoryapp.models

data class Cabang(
    val id: String = "",
    val nama: String = "",
    val alamat: String = "",
    val telepon: String = "",
    val userId: String = ""
)

// Pegawai.kt
package com.example.inventoryapp.models

data class Pegawai(
    val id: String = "",
    val nama: String = "",
    val posisi: String = "",
    val cabangId: String = "",
    val telepon: String = "",
    val email: String = "",
    val userId: String = ""
)