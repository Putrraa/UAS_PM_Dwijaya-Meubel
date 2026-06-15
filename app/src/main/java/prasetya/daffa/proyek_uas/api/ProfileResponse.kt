package prasetya.daffa.proyek_uas.api

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: ProfileUser? = null
)

data class ProfileUser(
    val id: Int = 0,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null
)

data class RiwayatPesananResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<RiwayatPesananApiItem> = emptyList()
)

data class RiwayatPesananApiItem(
    val id: Int = 0,

    @SerializedName("no_pesanan")
    val noPesanan: String? = null,

    val tanggal: String? = null,
    val total: String? = null,
    val status: String? = null
)

data class CustomOrderResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<CustomOrderApiItem> = emptyList()
)

data class CustomOrderApiItem(
    val id: Int = 0,

    @SerializedName("furniture_nama")
    val furnitureNama: String? = null,

    val kayu: String? = null,
    val ukuran: String? = null,
    val harga: String? = null,
    val status: String? = null,

    @SerializedName("gambar_url")
    val gambarUrl: String? = null
)