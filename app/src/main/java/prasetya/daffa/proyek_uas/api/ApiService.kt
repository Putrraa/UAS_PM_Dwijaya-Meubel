package prasetya.daffa.proyek_uas.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @FormUrlEncoded
    @POST("api/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<AuthResponse>

    @FormUrlEncoded
    @POST("api/register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<AuthResponse>

    @GET("api/kategori")
    fun getKategori(): Call<KategoriResponse>

    @GET("api/bahan")
    fun getBahan(): Call<BahanResponse>

    @GET("api/barang")
    fun getBarang(): Call<BarangListResponse>

    @GET("api/barang/{id}")
    fun getDetailBarang(
        @Path("id") id: Int
    ): Call<BarangResponse>

    @Multipart
    @POST("api/barang/store")
    fun tambahBarang(
        @Part("nama_barang") namaBarang: RequestBody,
        @Part("kategori_id") kategoriId: RequestBody,
        @Part("bahan_id") bahanId: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part("ukuran") ukuran: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part gambar: MultipartBody.Part
    ): Call<BarangResponse>

    @Multipart
    @POST("api/barang/update/{id}")
    fun updateBarang(
        @Path("id") id: Int,
        @Part("nama_barang") namaBarang: RequestBody,
        @Part("kategori_id") kategoriId: RequestBody,
        @Part("bahan_id") bahanId: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part("ukuran") ukuran: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody,
        @Part gambar: MultipartBody.Part? = null
    ): Call<BarangResponse>

    @POST("api/barang/delete/{id}")
    fun deleteBarang(
        @Path("id") id: Int
    ): Call<ResponseDefault>

    @GET("api/profile/{id}")
    fun getProfile(
        @Path("id") id: Int
    ): Call<ProfileResponse>

    @GET("api/riwayat-pesanan/{userId}")
    fun getRiwayatPesanan(
        @Path("userId") userId: Int
    ): Call<RiwayatPesananResponse>

    @GET("api/custom-order/{userId}")
    fun getCustomOrder(
        @Path("userId") userId: Int
    ): Call<CustomOrderResponse>

    @GET("api/laporan")
    fun getLaporan(): Call<LaporanResponse>

    @GET("api/pengguna")
    fun getPengguna(): Call<PenggunaResponse>

    @FormUrlEncoded
    @POST("api/pengguna/store")
    fun tambahPengguna(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("role") role: String
    ): Call<ResponseDefault>

    @FormUrlEncoded
    @POST("api/pengguna/update/{id}")
    fun updatePengguna(
        @Path("id") id: Int,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("role") role: String
    ): Call<ResponseDefault>

    @POST("api/pengguna/delete/{id}")
    fun deletePengguna(
        @Path("id") id: Int
    ): Call<ResponseDefault>

    @Multipart
    @POST("api/custom-order/store")
    fun tambahCustomOrder(
        @Part("user_id") userId: RequestBody,
        @Part("jenis_furniture") jenisFurniture: RequestBody,
        @Part("jenis_kayu") jenisKayu: RequestBody,
        @Part("ukuran") ukuran: RequestBody,
        @Part("catatan") catatan: RequestBody,
        @Part gambar: MultipartBody.Part?
    ): Call<ResponseDefault>

    @GET("api/keranjang/{userId}")
    fun getKeranjang(
        @Path("userId") userId: Int
    ): Call<KeranjangResponse>

    @FormUrlEncoded
    @POST("api/keranjang/update/{id}")
    fun updateJumlahKeranjang(
        @Path("id") id: Int,
        @Field("jumlah") jumlah: Int
    ): Call<ResponseDefault>

    @POST("api/keranjang/delete/{id}")
    fun hapusKeranjang(
        @Path("id") id: Int
    ): Call<ResponseDefault>

    @POST("api/keranjang/bayar/{userId}")
    fun bayarKeranjang(
        @Path("userId") userId: Int
    ): Call<ResponseDefault>

    @GET("api/kasir/custom-order")
    fun getKasirCustomOrder(): Call<KasirCustomOrderResponse>

    @FormUrlEncoded
    @POST("api/kasir/custom-order/update/{id}")
    fun updateKasirCustomOrder(
        @Path("id") id: Int,
        @Field("estimasi_harga") estimasiHarga: String,
        @Field("status") status: String
    ): Call<ResponseDefault>

    @GET("api/kasir/pesanan")
    fun getKasirPesanan(): Call<KasirPesananResponse>

    @FormUrlEncoded
    @POST("api/kasir/pesanan/update-status/{id}")
    fun updateStatusPesananKasir(
        @Path("id") id: Int,
        @Field("status") status: Int
    ): Call<ResponseDefault>


}