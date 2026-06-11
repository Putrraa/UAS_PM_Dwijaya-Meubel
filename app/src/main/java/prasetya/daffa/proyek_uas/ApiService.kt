package prasetya.daffa.proyek_uas.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @GET("api/kategori")
    fun getKategori(): Call<KategoriResponse>

    @GET("api/bahan")
    fun getBahan(): Call<BahanResponse>

    @GET("api/barang")
    fun getBarang(): Call<BarangListResponse>

    @GET("api/barang/{id}")
    fun getDetailBarang(@Path("id") id: Int): Call<BarangResponse>

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
    fun deleteBarang(@Path("id") id: Int): Call<BarangResponse>
}