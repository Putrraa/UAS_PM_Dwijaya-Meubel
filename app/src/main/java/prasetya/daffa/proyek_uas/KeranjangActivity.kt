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
import android.content.Intent
import android.net.Uri
import prasetya.daffa.proyek_uas.api.PaymentResponse
import prasetya.daffa.proyek_uas.databinding.ActivityKeranjangBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import androidx.appcompat.app.AlertDialog
import prasetya.daffa.proyek_uas.databinding.DialogCheckoutBinding

class KeranjangActivity : AppCompatActivity() {

    private lateinit var b: ActivityKeranjangBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: KeranjangAdapter

    private val listKeranjang = mutableListOf<KeranjangItem>()
    private var habisBukaMidtrans = false

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
    override fun onResume() {
        super.onResume()

        if (habisBukaMidtrans) {
            habisBukaMidtrans = false
            loadKeranjang()
            showToast("Memperbarui status pembayaran...", Toast.LENGTH_SHORT)
        }
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

            tampilkanDialogCheckout()
        }
    }

    private fun loadKeranjang() {
        val userId = session.getUserId()

        if (userId == 0) {
            showToast("User belum login atau ID user tidak ditemukan", Toast.LENGTH_LONG)
            return
        }

        ApiClient.instance.getKeranjang(userId).enqueue(object : Callback<KeranjangResponse> {
            override fun onResponse(
                call: Call<KeranjangResponse>,
                response: Response<KeranjangResponse>
            ) {
                if (call.isCanceled || !isActivitySafe()) return

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    val data = body.data.orEmpty()

                    listKeranjang.clear()
                    listKeranjang.addAll(data)

                    adapter.notifyDataSetChanged()

                    data.forEach {
                        android.util.Log.d("GAMBAR_KERANJANG", "URL: ${it.gambar_url}")
                    }

                    updateTotal()
                    updateEmptyState()
                } else {
                    val error = response.errorBody()?.string()

                    android.util.Log.e("KERANJANG_API", "Code: ${response.code()}")
                    android.util.Log.e("KERANJANG_API", "Error body: $error")
                    android.util.Log.e("KERANJANG_API", "Body: $body")

                    showToast("Gagal mengambil data. Code: ${response.code()}", Toast.LENGTH_LONG)
                }
            }

            override fun onFailure(call: Call<KeranjangResponse>, t: Throwable) {
                if (call.isCanceled || !isActivitySafe()) return

                showToast("Koneksi gagal: ${t.message}", Toast.LENGTH_LONG)
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
                    if (call.isCanceled || !isActivitySafe()) return

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        loadKeranjang()
                    } else {
                        showToast(
                            body?.message ?: "Gagal update jumlah",
                            Toast.LENGTH_SHORT
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    if (call.isCanceled || !isActivitySafe()) return

                    showToast("Koneksi gagal: ${t.message}", Toast.LENGTH_LONG)
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
                    if (call.isCanceled || !isActivitySafe()) return

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        showToast(body.message, Toast.LENGTH_SHORT)
                        loadKeranjang()
                    } else {
                        showToast(
                            body?.message ?: "Gagal hapus item",
                            Toast.LENGTH_SHORT
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    if (call.isCanceled || !isActivitySafe()) return

                    showToast("Koneksi gagal: ${t.message}", Toast.LENGTH_LONG)
                }
            })
    }

    private fun bayarSekarang(
        namaPenerima: String,
        noTelepon: String,
        alamat: String,
        kota: String,
        kodePos: String,
        catatan: String
    ) {
        val userId = session.getUserId()

        if (userId == 0) {
            showToast("User belum login atau ID user tidak ditemukan", Toast.LENGTH_LONG)
            return
        }

        b.btnBayarSekarang.isEnabled = false
        b.btnBayarSekarang.text = "MEMPROSES..."

        ApiClient.instance.bayarKeranjang(
            userId = userId,
            namaPenerima = namaPenerima,
            noTelepon = noTelepon,
            alamat = alamat,
            kota = kota,
            kodePos = kodePos,
            catatan = catatan
        ).enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(
                call: Call<PaymentResponse>,
                response: Response<PaymentResponse>
            ) {
                if (call.isCanceled || !isActivitySafe()) return

                b.btnBayarSekarang.isEnabled = true
                b.btnBayarSekarang.text = "BAYAR SEKARANG"

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    val redirectUrl = body.redirect_url

                    if (!redirectUrl.isNullOrEmpty()) {
                        habisBukaMidtrans = true
                        bukaMidtrans(redirectUrl)
                    } else {
                        showToast("URL pembayaran kosong", Toast.LENGTH_LONG)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MIDTRANS_API", "Code: ${response.code()}")
                    android.util.Log.e("MIDTRANS_API", "Error: $errorBody")

                    showToast(
                        body?.message ?: "Gagal membuat pembayaran. Code: ${response.code()}",
                        Toast.LENGTH_LONG
                    )
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                if (call.isCanceled || !isActivitySafe()) return

                b.btnBayarSekarang.isEnabled = true
                b.btnBayarSekarang.text = "BAYAR SEKARANG"

                showToast("Koneksi gagal: ${t.message}", Toast.LENGTH_LONG)
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
    private fun bukaMidtrans(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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
    private fun tampilkanDialogCheckout() {
        val dialogBinding = DialogCheckoutBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Bayar", null)
            .setNegativeButton("Batal", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val namaPenerima = dialogBinding.edtNamaPenerima.text.toString().trim()
                        val noTelepon = dialogBinding.edtNoTelepon.text.toString().trim()
                        val alamat = dialogBinding.edtAlamat.text.toString().trim()
                        val kota = dialogBinding.edtKota.text.toString().trim()
                        val kodePos = dialogBinding.edtKodePos.text.toString().trim()
                        val catatan = dialogBinding.edtCatatan.text.toString().trim()

                        if (namaPenerima.isEmpty()) {
                            dialogBinding.edtNamaPenerima.error = "Nama wajib diisi"
                            return@setOnClickListener
                        }

                        if (noTelepon.isEmpty()) {
                            dialogBinding.edtNoTelepon.error = "No telepon wajib diisi"
                            return@setOnClickListener
                        }

                        if (alamat.isEmpty()) {
                            dialogBinding.edtAlamat.error = "Alamat wajib diisi"
                            return@setOnClickListener
                        }

                        if (kota.isEmpty()) {
                            dialogBinding.edtKota.error = "Kota wajib diisi"
                            return@setOnClickListener
                        }

                        if (kodePos.isEmpty()) {
                            dialogBinding.edtKodePos.error = "Kode pos wajib diisi"
                            return@setOnClickListener
                        }

                        dismiss()

                        bayarSekarang(
                            namaPenerima = namaPenerima,
                            noTelepon = noTelepon,
                            alamat = alamat,
                            kota = kota,
                            kodePos = kodePos,
                            catatan = catatan
                        )
                    }
                }

                show()
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
    private fun isActivitySafe(): Boolean {
        return !isFinishing && !isDestroyed
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (!isActivitySafe()) return
        Toast.makeText(this, message, duration).show()
    }
    private fun formatRupiah(value: Int): String {
        val localeId = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeId)
        return formatter.format(value).replace(",00", "")
    }
}