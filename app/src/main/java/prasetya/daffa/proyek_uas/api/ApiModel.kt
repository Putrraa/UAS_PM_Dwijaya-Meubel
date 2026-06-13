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
    val message: String?,
    val data: List<Laporan>
)

data class Laporan(
    val id: Int?,

    @com.google.gson.annotations.SerializedName("jenis_pesanan")
    val jenisPesanan: String?,

    val kode: String?,

    val tanggal: String?,

    val pembeli: String?,

    @com.google.gson.annotations.SerializedName("total_harga")
    val totalHarga: Int?
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

data class KeranjangResponse(
    val status: Boolean,
    val message: String?,
    val data: List<KeranjangItem>
)

data class KeranjangItem(
    val id: Int,
    val barang_id: Int?,
    val nama_barang: String?,
    val harga: String?,
    val jumlah: Int?,
    val subtotal: String?,
    val gambar_url: String?
)

