package prasetya.daffa.proyek_uas.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("api/kategori")
    fun getKategoriRaw(): Call<ResponseBody>

    @GET("api/bahan")
    fun getBahanRaw(): Call<ResponseBody>
}