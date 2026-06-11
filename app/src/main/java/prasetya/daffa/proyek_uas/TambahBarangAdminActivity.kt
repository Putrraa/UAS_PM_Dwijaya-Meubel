package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Bahan
import prasetya.daffa.proyek_uas.api.BahanResponse
import prasetya.daffa.proyek_uas.api.Kategori
import prasetya.daffa.proyek_uas.api.KategoriResponse
import prasetya.daffa.proyek_uas.databinding.ActivityTambahBarangAdminBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahBarangAdminActivity : AppCompatActivity() {

    private lateinit var b: ActivityTambahBarangAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTambahBarangAdminBinding.inflate(layoutInflater)
        setContentView(b.root)

        // TEST RAW RESPONSE KATEGORI
        testKategoriRaw()
    }

    private fun testKategoriRaw() {
        ApiClient.instance.getKategoriRaw()
            .enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {

                override fun onResponse(
                    call: retrofit2.Call<okhttp3.ResponseBody>,
                    response: retrofit2.Response<okhttp3.ResponseBody>
                ) {
                    val hasil = if (response.isSuccessful) {
                        response.body()?.string()
                    } else {
                        response.errorBody()?.string()
                    }

                    android.util.Log.e("CEK_KATEGORI", hasil ?: "Kosong")

                    Toast.makeText(
                        this@TambahBarangAdminActivity,
                        hasil?.take(150) ?: "Response kosong",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onFailure(
                    call: retrofit2.Call<okhttp3.ResponseBody>,
                    t: Throwable
                ) {
                    android.util.Log.e("CEK_KATEGORI", "Error", t)

                    Toast.makeText(
                        this@TambahBarangAdminActivity,
                        "Gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}