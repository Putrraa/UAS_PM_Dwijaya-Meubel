package prasetya.daffa.proyek_uas.api

data class ResponseDefault(
    val status: Boolean,
    val message: String
)

data class KategoriResponse(
    val status: Boolean,
    val message: String?,
    val data: List<Kategori>
)

data class Kategori(
    val id: Int,
    val nama_kategori: String,
    val gambar: String?,
    val gambar_url: String?
)

data class BahanResponse(
    val status: Boolean,
    val message: String?,
    val data: List<Bahan>
)

data class Bahan(
    val id: Int,
    val nama_bahan: String
)

data class BarangListResponse(
    val status: Boolean,
    val message: String,
    val data: List<Barang>
)

data class BarangResponse(
    val status: Boolean,
    val message: String,
    val data: Barang?,
    val gambar_url: String?
)

data class Barang(
    val id: Int?,
    val nama_barang: String?,
    val kategori_id: Int?,
    val bahan_id: Int?,
    val harga: String?,
    val stok: Int?,
    val ukuran: String?,
    val deskripsi: String?,
    val gambar: String?,
    val gambar_url: String?,
    val kategori: Kategori?,
    val bahan: Bahan?
)
data class LaporanResponse(
    val status: Boolean,
    val message: String,
    val data: List<Laporan>
)

data class Laporan(
    val id: Int?,
    val jenis: String?,
    val kode: String?,
    val nama_customer: String?,
    val tanggal: String?,
    val total: String?,
    val status: String?
)

data class PenggunaResponse(
    val status: Boolean,
    val message: String,
    val data: List<Pengguna>
)

data class Pengguna(
    val id: Int?,
    val name: String?,
    val email: String?,
    val role: String?
)