package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import prasetya.daffa.proyek_uas.adapter.KeranjangAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.KeranjangItem
import prasetya.daffa.proyek_uas.api.KeranjangResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.ActivityKeranjangBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class KeranjangActivity : AppCompatActivity() {

    private lateinit var b: ActivityKeranjangBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: KeranjangAdapter

    private val listKeranjang = mutableListOf<KeranjangItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityKeranjangBinding.inflate(layoutInflater)
        setContentView(b.root)
        enableEdgeToEdge()

        session = SessionManager(this)

        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        b.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupButton()

        loadKeranjang()
    }

    private fun setupRecyclerView() {
        adapter = KeranjangAdapter(
            listKeranjang,
            onTambah = { item ->
                val jumlahBaru = (item.jumlah ?: 0) + 1
                updateJumlah(item.id, jumlahBaru)
            },
            onKurang = { item ->
                val jumlahSekarang = item.jumlah ?: 0

                if (jumlahSekarang <= 1) {
                    Toast.makeText(this, "Jumlah minimal 1", Toast.LENGTH_SHORT).show()
                } else {
                    updateJumlah(item.id, jumlahSekarang - 1)
                }
            },
            onHapus = { item ->
                hapusItem(item.id)
            }
        )

        b.recyclerViewKeranjang.layoutManager = LinearLayoutManager(this)
        b.recyclerViewKeranjang.adapter = adapter
    }

    private fun setupButton() {
        b.btnKembaliBlanja.setOnClickListener {
            finish()
        }

        b.btnBayarSekarang.setOnClickListener {
            if (listKeranjang.isEmpty()) {
                Toast.makeText(this, "Keranjang masih kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            bayarSekarang()
        }
    }

    private fun loadKeranjang() {
        val userId = session.getUserId()

        if (userId == 0) {
            Toast.makeText(this, "User belum login atau ID user tidak ditemukan", Toast.LENGTH_LONG).show()
            return
        }

        ApiClient.instance.getKeranjang(userId).enqueue(object : Callback<KeranjangResponse> {
            override fun onResponse(
                call: Call<KeranjangResponse>,
                response: Response<KeranjangResponse>
            ) {
                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    listKeranjang.clear()
                    listKeranjang.addAll(body.data)

                    adapter.notifyDataSetChanged()

                    body.data.forEach {
                        android.util.Log.d("GAMBAR_KERANJANG", "URL: ${it.gambar_url}")
                    }

                    updateTotal()
                    updateEmptyState()
                } else {
                    val error = response.errorBody()?.string()

                    android.util.Log.e("KERANJANG_API", "Code: ${response.code()}")
                    android.util.Log.e("KERANJANG_API", "Error body: $error")

                    Toast.makeText(
                        this@KeranjangActivity,
                        "Gagal mengambil data. Code: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()

                    android.util.Log.e("KERANJANG_API", "Code: ${response.code()}")
                    android.util.Log.e("KERANJANG_API", "Error: $error")
                    android.util.Log.e("KERANJANG_API", "Body: $body")
                }
            }

            override fun onFailure(call: Call<KeranjangResponse>, t: Throwable) {
                Toast.makeText(
                    this@KeranjangActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()

                android.util.Log.e("KERANJANG_API", "Failure: ${t.message}")
            }
        })
    }

    private fun updateJumlah(detailId: Int, jumlah: Int) {
        ApiClient.instance.updateJumlahKeranjang(detailId, jumlah)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        loadKeranjang()
                    } else {
                        Toast.makeText(
                            this@KeranjangActivity,
                            body?.message ?: "Gagal update jumlah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    Toast.makeText(
                        this@KeranjangActivity,
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun hapusItem(detailId: Int) {
        ApiClient.instance.hapusKeranjang(detailId)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        Toast.makeText(
                            this@KeranjangActivity,
                            body.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        loadKeranjang()
                    } else {
                        Toast.makeText(
                            this@KeranjangActivity,
                            body?.message ?: "Gagal hapus item",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    Toast.makeText(
                        this@KeranjangActivity,
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun bayarSekarang() {
        val userId = session.getUserId()

        b.btnBayarSekarang.isEnabled = false
        b.btnBayarSekarang.text = "MEMPROSES..."

        ApiClient.instance.bayarKeranjang(userId)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    b.btnBayarSekarang.isEnabled = true
                    b.btnBayarSekarang.text = "BAYAR SEKARANG"

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        Toast.makeText(
                            this@KeranjangActivity,
                            body.message,
                            Toast.LENGTH_LONG
                        ).show()

                        loadKeranjang()
                    } else {
                        Toast.makeText(
                            this@KeranjangActivity,
                            body?.message ?: "Gagal memproses pembayaran",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    b.btnBayarSekarang.isEnabled = true
                    b.btnBayarSekarang.text = "BAYAR SEKARANG"

                    Toast.makeText(
                        this@KeranjangActivity,
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun updateTotal() {
        val total = listKeranjang.sumOf { item ->
            val harga = item.harga.toHargaInt()
            val jumlah = item.jumlah ?: 0
            harga * jumlah
        }

        b.tvTotalPembayaran.text = formatRupiah(total)
    }

    private fun updateEmptyState() {
        if (listKeranjang.isEmpty()) {
            b.layoutKeranjangKosong.visibility = android.view.View.VISIBLE
            b.recyclerViewKeranjang.visibility = android.view.View.GONE
        } else {
            b.layoutKeranjangKosong.visibility = android.view.View.GONE
            b.recyclerViewKeranjang.visibility = android.view.View.VISIBLE
        }
    }

    private fun String?.toHargaInt(): Int {
        return this
            ?.replace("Rp", "")
            ?.replace(".", "")
            ?.replace(",", "")
            ?.trim()
            ?.toIntOrNull() ?: 0
    }

    private fun formatRupiah(value: Int): String {
        val localeId = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeId)
        return formatter.format(value).replace(",00", "")
    }
}