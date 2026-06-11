package prasetya.daffa.proyek_uas.api


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
    val gambar_url: String?
)