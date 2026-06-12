package prasetya.daffa.proyek_uas.api

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    val status: Boolean,
    val message: String,
    val data: ProfileUser?
)

data class ProfileUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

data class RiwayatPesananResponse(
    val status: Boolean,
    val message: String,
    val data: List<RiwayatPesananApiItem>
)

data class RiwayatPesananApiItem(
    val id: Int,

    @SerializedName("no_pesanan")
    val noPesanan: String,

    val tanggal: String,
    val total: String,
    val status: String
)

data class CustomOrderResponse(
    val status: Boolean,
    val message: String,
    val data: List<CustomOrderApiItem>
)

data class CustomOrderApiItem(
    val id: Int,

    @SerializedName("furniture_nama")
    val furnitureNama: String,

    val kayu: String,
    val ukuran: String,
    val harga: String,
    val status: String
)