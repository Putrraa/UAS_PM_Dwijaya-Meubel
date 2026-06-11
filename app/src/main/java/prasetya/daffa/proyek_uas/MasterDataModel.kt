package prasetya.daffa.proyek_uas.api

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