package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import prasetya.daffa.proyek_uas.adapter.KeranjangAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.KeranjangItem
import prasetya.daffa.proyek_uas.api.KeranjangResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.api.PaymentResponse
import prasetya.daffa.proyek_uas.api.PaymentStatusResponse
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
    private var pendingPaymentOrderId: String? = null
    private val paymentStatusHandler = Handler(Looper.getMainLooper())
    private val paymentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val orderId = pendingPaymentOrderId

            if (!orderId.isNullOrEmpty()) {
                cekStatusPembayaran(orderId, 1)
            } else {
                loadKeranjang()
            }
        }

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
            val orderId = pendingPaymentOrderId
            if (!orderId.isNullOrEmpty()) {
                cekStatusPembayaran(orderId, 1)
            } else {
                loadKeranjang()
                showToast("Memperbarui status pembayaran...", Toast.LENGTH_SHORT)
            }
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
                    android.util.Log.d("CEK_MIDTRANS", "order_id = ${body.order_id}")
                    android.util.Log.d("CEK_MIDTRANS", "redirect_url = ${body.redirect_url}")
                    android.util.Log.d("CEK_MIDTRANS", "message = ${body.message}")

                    if (!redirectUrl.isNullOrEmpty()) {
                        pendingPaymentOrderId = body.order_id
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
        val intent = Intent(this, PaymentWebViewActivity::class.java).apply {
            putExtra(PaymentWebViewActivity.EXTRA_PAYMENT_URL, url)
        }
        paymentLauncher.launch(intent)
    }

    private fun cekStatusPembayaran(orderId: String, percobaan: Int = 1) {
        if (percobaan == 1) {
            showToast("Memperbarui status pembayaran...", Toast.LENGTH_SHORT)
        }

        ApiClient.instance.cekStatusPembayaran(orderId)
            .enqueue(object : Callback<PaymentStatusResponse> {
                override fun onResponse(
                    call: Call<PaymentStatusResponse>,
                    response: Response<PaymentStatusResponse>
                ) {
                    if (call.isCanceled || !isActivitySafe()) return

                    val paymentStatus = response.body()?.payment_status.orEmpty()

                    when (paymentStatus.lowercase(Locale.getDefault())) {
                        "paid", "settlement", "capture", "success" -> {
                            pendingPaymentOrderId = null
                            paymentStatusHandler.removeCallbacksAndMessages(null)
                            showToast("Pembayaran berhasil", Toast.LENGTH_LONG)
                            loadKeranjang()
                        }
                        "pending" -> {
                            loadKeranjangSetelahPending()
                        }
                        "deny", "expire", "cancel", "failed", "failure" -> {
                            pendingPaymentOrderId = null
                            paymentStatusHandler.removeCallbacksAndMessages(null)
                            showToast("Pembayaran gagal atau dibatalkan", Toast.LENGTH_LONG)
                            loadKeranjang()
                        }
                        else -> {
                            if (percobaan < MAX_PAYMENT_STATUS_RETRY) {
                                jadwalkanCekStatusUlang(orderId, percobaan)
                            } else {
                                loadKeranjangSetelahPending()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PaymentStatusResponse>, t: Throwable) {
                    if (call.isCanceled || !isActivitySafe()) return

                    if (percobaan < MAX_PAYMENT_STATUS_RETRY) {
                        jadwalkanCekStatusUlang(orderId, percobaan)
                    } else {
                        showToast("Gagal cek status pembayaran: ${t.message}", Toast.LENGTH_LONG)
                        loadKeranjang()
                    }
                }
            })
    }

    private fun jadwalkanCekStatusUlang(orderId: String, percobaan: Int) {
        paymentStatusHandler.postDelayed({
            if (isActivitySafe()) {
                cekStatusPembayaran(orderId, percobaan + 1)
            }
        }, PAYMENT_STATUS_RETRY_DELAY_MS)
    }

    private fun loadKeranjangSetelahPending() {
        val userId = session.getUserId()

        if (userId == 0) {
            showToast("Pembayaran masih pending", Toast.LENGTH_LONG)
            return
        }

        ApiClient.instance.getKeranjang(userId).enqueue(object : Callback<KeranjangResponse> {
            override fun onResponse(
                call: Call<KeranjangResponse>,
                response: Response<KeranjangResponse>
            ) {
                if (call.isCanceled || !isActivitySafe()) return

                val data = response.body()?.data.orEmpty()
                listKeranjang.clear()
                listKeranjang.addAll(data)
                adapter.notifyDataSetChanged()
                updateTotal()
                updateEmptyState()

                if (data.isEmpty()) {
                    showToast("Pesanan berhasil dibuat", Toast.LENGTH_LONG)
                } else {
                    showToast("Pembayaran masih pending", Toast.LENGTH_LONG)
                }
            }

            override fun onFailure(call: Call<KeranjangResponse>, t: Throwable) {
                if (call.isCanceled || !isActivitySafe()) return

                showToast("Pembayaran masih diproses", Toast.LENGTH_LONG)
                loadKeranjang()
            }
        })
    }

    private fun kosongkanTampilanKeranjang() {
        listKeranjang.clear()
        adapter.notifyDataSetChanged()
        updateTotal()
        updateEmptyState()
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

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnBatalCheckout.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnBayarCheckout.setOnClickListener {
            val namaPenerima = dialogBinding.edtNamaPenerima.text.toString().trim()
            val noTelepon = dialogBinding.edtNoTelepon.text.toString().trim()
            val alamat = dialogBinding.edtAlamat.text.toString().trim()
            val kota = dialogBinding.edtKota.text.toString().trim()
            val kodePos = dialogBinding.edtKodePos.text.toString().trim()
            val catatan = dialogBinding.edtCatatan.text.toString().trim()

            if (namaPenerima.isEmpty()) {
                dialogBinding.edtNamaPenerima.error = "Nama wajib diisi"
                dialogBinding.edtNamaPenerima.requestFocus()
                return@setOnClickListener
            }

            if (noTelepon.isEmpty()) {
                dialogBinding.edtNoTelepon.error = "No telepon wajib diisi"
                dialogBinding.edtNoTelepon.requestFocus()
                return@setOnClickListener
            }

            if (alamat.isEmpty()) {
                dialogBinding.edtAlamat.error = "Alamat wajib diisi"
                dialogBinding.edtAlamat.requestFocus()
                return@setOnClickListener
            }

            if (kota.isEmpty()) {
                dialogBinding.edtKota.error = "Kota wajib diisi"
                dialogBinding.edtKota.requestFocus()
                return@setOnClickListener
            }

            if (kodePos.isEmpty()) {
                dialogBinding.edtKodePos.error = "Kode pos wajib diisi"
                dialogBinding.edtKodePos.requestFocus()
                return@setOnClickListener
            }

            dialog.dismiss()

            bayarSekarang(
                namaPenerima = namaPenerima,
                noTelepon = noTelepon,
                alamat = alamat,
                kota = kota,
                kodePos = kodePos,
                catatan = catatan
            )
        }

        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.92f).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.82f).toInt()

            dialog.window?.setLayout(width, height)
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        dialog.show()
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

    override fun onDestroy() {
        paymentStatusHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
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

    companion object {
        private const val MAX_PAYMENT_STATUS_RETRY = 2
        private const val PAYMENT_STATUS_RETRY_DELAY_MS = 700L
    }
}
