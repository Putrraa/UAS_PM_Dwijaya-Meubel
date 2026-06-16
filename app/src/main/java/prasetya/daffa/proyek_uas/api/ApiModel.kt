package prasetya.daffa.proyek_uas.api

data class ResponseDefault(
    val status: Boolean,
    val message: String
)

data class KategoriResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<Kategori> = emptyList()
)

data class Kategori(
    val id: Int = 0,
    val nama_kategori: String? = null,
    val gambar: String? = null,
    val gambar_url: String? = null
)

data class BahanResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<Bahan> = emptyList()
)

data class Bahan(
    val id: Int = 0,
    val nama_bahan: String? = null
)

data class BarangListResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<Barang> = emptyList()
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
    val status: Boolean = false,
    val message: String? = null,
    val data: List<Laporan> = emptyList()
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
    val status: Boolean = false,
    val message: String? = null,
    val data: List<Pengguna> = emptyList()
)

data class Pengguna(
    val id: Int?,
    val name: String?,
    val email: String?,
    val role: String?,

    @com.google.gson.annotations.SerializedName("created_at")
    val createdAt: String?
)
data class PaymentResponse(
    val status: Boolean? = false,
    val message: String? = null,
    val order_id: String? = null,
    val snap_token: String? = null,
    val redirect_url: String? = null
)
data class KeranjangResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<KeranjangItem> = emptyList()
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

data class KasirCustomOrderResponse(
    val status: Boolean,
    val message: String?,
    val data: List<KasirCustomOrderItem>
)

data class KasirCustomOrderItem(
    val id: Int?,

    val customer: String?,

    @com.google.gson.annotations.SerializedName("jenis_furniture")
    val jenisFurniture: String?,

    @com.google.gson.annotations.SerializedName("jenis_kayu")
    val jenisKayu: String?,

    val ukuran: String?,
    val catatan: String?,

    @com.google.gson.annotations.SerializedName("gambar_url")
    val gambarUrl: String?,

    @com.google.gson.annotations.SerializedName("estimasi_harga")
    val estimasiHarga: Int?,

    val harga: String?,

    val status: String?,

    @com.google.gson.annotations.SerializedName("status_label")
    val statusLabel: String?
)

data class KasirPesananResponse(
    val status: Boolean,
    val message: String?,
    val data: List<KasirPesananItem>
)

data class KasirPesananItem(
    val id: Int?,

    val kode: String?,

    val tanggal: String?,

    @com.google.gson.annotations.SerializedName("nama_penerima")
    val namaPenerima: String?,

    @com.google.gson.annotations.SerializedName("no_telepon")
    val noTelepon: String?,

    val alamat: String?,
    val kota: String?,

    @com.google.gson.annotations.SerializedName("kode_pos")
    val kodePos: String?,

    @com.google.gson.annotations.SerializedName("jumlah_harga")
    val jumlahHarga: Int?,

    val total: String?,

    @com.google.gson.annotations.SerializedName("metode_pembayaran")
    val metodePembayaran: String?,

    @com.google.gson.annotations.SerializedName("metode_label")
    val metodeLabel: String?,

    val status: Int?,

    @com.google.gson.annotations.SerializedName("status_label")
    val statusLabel: String?
)

