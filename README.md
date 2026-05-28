# 📦 RuangRupa POS (Aplikasi Penjualan)

Aplikasi **Point‑of‑Sale (POS)** modern untuk Android, dibangun dengan Kotlin, arsitektur MVVM, dan Firebase. Menyediakan UI premium, pencetakan receipt lewat Bluetooth ESC/POS, serta berbagi receipt sebagai gambar.

---

## ✨ Fitur Utama
- **UI seragam**: kartu produk dan karyawan memiliki desain yang sama (margin, radius, elevasi). 
- **Filter kategori**: pilih chip kategori, hanya barang di kategori tersebut yang ditampilkan.
- **Pemilihan pelanggan**: daftar pelanggan di‑load dari Firebase, dapat dipilih via dropdown.
- **Field pegawai kosong** bila tidak dipilih (tidak menampilkan nama default).
- **Penghapusan pajak** pada receipt (tetap dihitung secara internal).
- **Branding receipt**: mencetak logo aplikasi (`logo_pos`) dan teks **"ruangrupa"** di atas receipt.
- **Berbagi receipt**: export receipt sebagai PNG dan bagikan melalui intent.
- **Mode malam**: warna disesuaikan agar tidak muncul merah/pink pada tombol keluar.
- **Firebase Realtime Database** untuk produk, kategori, pelanggan, dan profil toko.

---

## 📚 Alur UI / Tampilan Aplikasi
1. **Login / Selamat Datang** – Tampilan sederhana yang responsif terhadap Night‑Mode.
2. **Beranda POS** – Daftar produk, filter kategori, kolom pencarian, selector pegawai.
3. **Keranjang & Checkout** – Daftar item dengan kuantitas, harga, subtotal, diskon, total (pajak tidak dicetak).
4. **Nota** – Tampilan nota lengkap dengan logo, brand **ruangrupa**, detail transaksi, dan tombol **Bagikan** serta **Cetak**.

---

## 🛠️ Instalasi & Setup
> **Prasyarat**
> * Android Studio (Flamingo atau lebih baru) dengan **Kotlin 1.9+**
> * SDK Android **33** (atau target 33)
> * Bluetooth ESC/POS printer (opsional untuk pencetakan)

**Langkah-langkah**
```bash
# 1. Clone repository
git clone https://github.com/username/ruangrupa-pos.git
cd ruangrupa-pos

# 2. Buka dengan Android Studio
#    File → Open → pilih folder proyek

# 3. Sinkronisasi Gradle
#    Klik “Sync Now” bila muncul prompt

# 4. Konfigurasi Firebase
#    - Tempatkan file google-services.json ke folder app/
#    - Pastikan Realtime Database memiliki node:
#      - profil
#      - kategori
#      - produk
#      - pelanggan
#      - transaksi

# 5. Build & Run
./gradlew assembleDebug   # atau gunakan tombol Run di Android Studio
```

---

## ▶️ Menjalankan Aplikasi
1. Pilih device atau emulator (API 33).
2. Aplikasi akan menampilkan daftar produk.
3. Pilih **kategori** di bagian atas untuk memfilter produk.
4. Tambahkan produk ke keranjang dengan mengetuknya.
5. Pilih **pelanggan** dari dropdown (jika ada). Jika tidak dipilih, field tetap kosong.
6. Tekan tombol **Pembayaran** untuk membuka layar receipt.
7. Pada layar receipt:
   - Tekan **Bagikan** untuk mengirim gambar receipt.
   - Tekan **Cetak** untuk mencetak via printer Bluetooth (logo + nama aplikasi **ruangrupa** akan tercetak).

---

## ⚙️ Detail Teknis
| Komponen | Implementasi |
|----------|--------------|
| **Arsitektur** | MVVM + LiveData |
| **UI** | XML layout, `ConstraintLayout`, `MaterialComponents`, `GradientDrawable` (glassmorphism) |
| **Cetak Bluetooth** | Library `com.dantsu:escposprinter:2.0.0`; `EscPosPrinter.printBitmap(..)` untuk logo, `printFormattedText(..)` untuk teks. |
| **Berbagi Gambar** | `FileProvider` (authority: `${applicationId}.provider`) untuk mengirim PNG. |
| **Night‑Mode** | Resource `values-night/colors.xml` dengan palet hangat; tidak ada warna merah/pink pada tombol logout. |
| **Lokalisasi** | `LocaleHelper` + string resources (`strings.xml`) berbahasa Indonesia. |
| **Dependensi Utama** | `implementation "com.dantsu:escposprinter:2.0.0"` dan `implementation "com.google.firebase:firebase-database-ktx"` |

---

## 🛠️ Kustomisasi & Perluasan
- **Menambahkan Pajak**: Edit `ReceiptActivity` footer untuk menampilkan `pajak`. 
- **Ubah Branding**: Ganti `logo_pos.png` di `res/drawable/` dan ubah teks `"ruangrupa"` di `doBluetoothPrint()`. 
- **Menambah Laporan**: Buat activity baru, ikuti pola `CetakActivity`. 
- **Penyesuaian UI**: Tambah animasi mikro pada tombol atau chip dengan `Animator` atau `MotionLayout`. 

---

## 📸 Screenshot & Asset
Letakkan gambar screenshot ke dalam folder `assets/` (atau `app/src/main/res/drawable/` bila ingin menjadi resource). Berikut placeholder yang sudah dimasukkan; ganti dengan gambar aktual Anda.
```markdown
![Login Screen](assets/login.png)
![Beranda POS](assets/home.png)
![Keranjang & Checkout](assets/checkout.png)
![Preview Nota](assets/nota_preview.png)
![Dialog Cetak Bluetooth](assets/print_dialog.png)
```
> **Catatan:** Gambar placeholder berada di `assets/placeholder_*.png` (hasil generate otomatis). Ganti dengan foto asli aplikasi Anda.

---

## 🤝 Kontribusi
1. Fork repository ini.
2. Buat branch fitur (`git checkout -b fitur/fitur-baru`).
3. Lakukan perubahan, pastikan kode mengikuti style project.
4. Buat Pull Request dengan deskripsi yang jelas.

---

## 📄 Lisensi
Proyek ini dilisensikan di bawah **MIT License** – lihat file `LICENSE` untuk detail lengkap.

---

*Selamat mencoba RuangRupa POS! Jika ada pertanyaan atau ingin menambahkan fitur, silakan buat issue atau hubungi saya.*
