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

    // =========================
    // AUTH
    // =========================
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


    // =========================
    // KATEGORI
    // =========================
    @GET("api/kategori")
    fun getKategori(): Call<KategoriResponse>

    @Multipart
    @POST("api/kategori/store")
    fun tambahKategori(
        @Part("nama_kategori") namaKategori: RequestBody,
        @Part gambar: MultipartBody.Part?
    ): Call<ResponseDefault>


    // =========================
    // BAHAN
    // =========================
    @GET("api/bahan")
    fun getBahan(): Call<BahanResponse>

    @FormUrlEncoded
    @POST("api/bahan/store")
    fun tambahBahan(
        @Field("nama_bahan") namaBahan: String
    ): Call<ResponseDefault>

    @FormUrlEncoded
    @POST("api/orders/bayar")
    fun bayarOrder(
        @Field("user_id") userId: Int,
        @Field("nama_penerima") namaPenerima: String,
        @Field("no_telepon") noTelepon: String,
        @Field("alamat") alamat: String,
        @Field("kota") kota: String,
        @Field("kode_pos") kodePos: String,
        @Field("catatan") catatan: String
    ): Call<PaymentResponse>
    // =========================
    // BARANG
    // =========================
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
        @Part gambar: MultipartBody.Part?
    ): Call<ResponseDefault>

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


    // =========================
    // PROFILE CUSTOMER
    // =========================
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


    // =========================
    // LAPORAN
    // =========================
    @GET("api/laporan")
    fun getLaporan(): Call<LaporanResponse>


    // =========================
    // PENGGUNA
    // =========================
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

    @FormUrlEncoded
    @POST("api/pengguna/update-password/{id}")
    fun updatePasswordPengguna(
        @Path("id") id: Int,
        @Field("password_lama") passwordLama: String,
        @Field("password_baru") passwordBaru: String,
        @Field("konfirmasi_password") konfirmasiPassword: String
    ): Call<ResponseDefault>


    // =========================
    // CUSTOM ORDER CUSTOMER
    // =========================
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


    // =========================
    // KERANJANG
    // =========================
    @GET("api/keranjang/{userId}")
    fun getKeranjang(
        @Path("userId") userId: Int
    ): Call<KeranjangResponse>

    @FormUrlEncoded
    @POST("api/keranjang/tambah")
    fun tambahKeranjangQty(
        @Field("user_id") userId: Int,
        @Field("barang_id") barangId: Int,
        @Field("jumlah") jumlah: Int
    ): Call<ResponseDefault>

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

    @FormUrlEncoded
    @POST("api/checkout/bayar")
    fun bayarKeranjang(
        @Field("user_id") userId: Int,
        @Field("nama_penerima") namaPenerima: String,
        @Field("no_telepon") noTelepon: String,
        @Field("alamat") alamat: String,
        @Field("kota") kota: String,
        @Field("kode_pos") kodePos: String,
        @Field("catatan") catatan: String
    ): Call<PaymentResponse>


    // =========================
    // KASIR CUSTOM ORDER
    // =========================
    @GET("api/kasir/custom-order")
    fun getKasirCustomOrder(): Call<KasirCustomOrderResponse>

    @FormUrlEncoded
    @POST("api/kasir/custom-order/update/{id}")
    fun updateKasirCustomOrder(
        @Path("id") id: Int,
        @Field("estimasi_harga") estimasiHarga: String,
        @Field("status") status: String
    ): Call<ResponseDefault>


    // =========================
    // KASIR PESANAN
    // =========================
    @GET("api/kasir/pesanan")
    fun getKasirPesanan(): Call<KasirPesananResponse>

    @FormUrlEncoded
    @POST("api/kasir/pesanan/update-status/{id}")
    fun updateStatusPesananKasir(
        @Path("id") id: Int,
        @Field("status") status: Int
    ): Call<ResponseDefault>
}