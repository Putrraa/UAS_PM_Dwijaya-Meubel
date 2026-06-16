package prasetya.daffa.proyek_uas

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.adapter.CustomOrder
import prasetya.daffa.proyek_uas.adapter.CustomOrderAdapter
import prasetya.daffa.proyek_uas.adapter.RiwayatPesanan
import prasetya.daffa.proyek_uas.adapter.RiwayatPesananAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.CustomOrderResponse
import prasetya.daffa.proyek_uas.api.PaymentResponse
import prasetya.daffa.proyek_uas.api.PaymentStatusResponse
import prasetya.daffa.proyek_uas.api.ProfileResponse
import prasetya.daffa.proyek_uas.api.RiwayatPesananResponse
import prasetya.daffa.proyek_uas.databinding.ActivityProfileBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileBinding
    private lateinit var session: SessionManager

    private lateinit var btnBack: ImageButton
    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var etNamaLengkap: EditText
    private lateinit var etEmail: EditText

    private lateinit var navProfilSaya: LinearLayout
    private lateinit var navRiwayatPesanan: LinearLayout
    private lateinit var navCustomOrder: LinearLayout

    private lateinit var sectionProfilSaya: LinearLayout
    private lateinit var sectionRiwayatPesanan: LinearLayout
    private lateinit var sectionCustomOrder: LinearLayout

    private lateinit var rvRiwayatPesanan: RecyclerView
    private lateinit var rvCustomOrder: RecyclerView

    private var pendingCustomPaymentOrderId: String? = null
    private val customPaymentStatusHandler = Handler(Looper.getMainLooper())

    private val customPaymentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val orderId = pendingCustomPaymentOrderId

            if (!orderId.isNullOrEmpty()) {
                cekStatusPembayaranCustom(orderId, 1)
            } else {
                loadCustomOrderFromDatabase()
                loadRiwayatPesananFromDatabase()
            }
        }

    private val allNavItems: List<LinearLayout>
        get() = listOf(
            navProfilSaya,
            navRiwayatPesanan,
            navCustomOrder
        )

    private val allSections: List<LinearLayout>
        get() = listOf(
            sectionProfilSaya,
            sectionRiwayatPesanan,
            sectionCustomOrder
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        if (!session.isLogin()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        window.statusBarColor = Color.parseColor("#EEF2EA")
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        bindViews()
        setupRecyclerView()
        setupClickListeners()

        showSection(0)

        loadProfileFromDatabase()
        loadRiwayatPesananFromDatabase()
        loadCustomOrderFromDatabase()
    }

    override fun onResume() {
        super.onResume()

        if (::session.isInitialized && session.isLogin()) {
            loadRiwayatPesananFromDatabase()
            loadCustomOrderFromDatabase()
        }
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        etNamaLengkap = findViewById(R.id.etNamaLengkap)
        etEmail = findViewById(R.id.etEmail)

        navProfilSaya = findViewById(R.id.navProfilSaya)
        navRiwayatPesanan = findViewById(R.id.navRiwayatPesanan)
        navCustomOrder = findViewById(R.id.navCustomOrder)

        sectionProfilSaya = findViewById(R.id.sectionProfilSaya)
        sectionRiwayatPesanan = findViewById(R.id.sectionRiwayatPesanan)
        sectionCustomOrder = findViewById(R.id.sectionCustomOrder)

        rvRiwayatPesanan = findViewById(R.id.rvRiwayatPesanan)
        rvCustomOrder = findViewById(R.id.rvCustomOrder)
    }

    private fun setupRecyclerView() {
        rvRiwayatPesanan.layoutManager = LinearLayoutManager(this)
        rvCustomOrder.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        navProfilSaya.setOnClickListener {
            showSection(0)
        }

        navRiwayatPesanan.setOnClickListener {
            showSection(1)
        }

        navCustomOrder.setOnClickListener {
            showSection(2)
        }
    }

    private fun isActivitySafe(): Boolean {
        return !isFinishing && !isDestroyed
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (!isActivitySafe()) return
        Toast.makeText(this, message, duration).show()
    }

    private fun loadProfileFromDatabase() {
        val userId = session.getUserId()

        ApiClient.instance.getProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                if (call.isCanceled || !isActivitySafe()) return

                val body = response.body()

                if (response.isSuccessful && body?.status == true && body.data != null) {
                    val user = body.data

                    val nama = user.name ?: "Customer"
                    val email = user.email ?: "customer@gmail.com"

                    tvProfileName.text = nama
                    tvProfileEmail.text = email
                    etNamaLengkap.setText(nama)
                    etEmail.setText(email)

                    tvAvatarInitial.text =
                        nama.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"
                } else {
                    tampilkanDataSession()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                if (call.isCanceled || !isActivitySafe()) return

                tampilkanDataSession()
                showToast("Gagal mengambil profile: ${t.message}", Toast.LENGTH_SHORT)
            }
        })
    }

    private fun tampilkanDataSession() {
        val nama = session.getName().ifEmpty { "Customer" }
        val email = session.getEmail().ifEmpty { "customer@gmail.com" }

        tvProfileName.text = nama
        tvProfileEmail.text = email
        etNamaLengkap.setText(nama)
        etEmail.setText(email)

        tvAvatarInitial.text =
            nama.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"
    }

    private fun loadRiwayatPesananFromDatabase() {
        val userId = session.getUserId()

        ApiClient.instance.getRiwayatPesanan(userId)
            .enqueue(object : Callback<RiwayatPesananResponse> {
                override fun onResponse(
                    call: Call<RiwayatPesananResponse>,
                    response: Response<RiwayatPesananResponse>
                ) {
                    if (call.isCanceled || !isActivitySafe()) return

                    val body = response.body()
                    val data = body?.data.orEmpty()

                    if (response.isSuccessful && body?.status == true) {
                        val dataPesanan = data.map { item ->
                            RiwayatPesanan(
                                noPesanan = item.noPesanan ?: "-",
                                tanggal = item.tanggal ?: "-",
                                total = item.total ?: "Rp 0",
                                status = item.status ?: "-",
                                metodePembayaran = item.metodeLabel
                                    ?: formatMetodePembayaran(item.metodePembayaran)
                            )
                        }

                        rvRiwayatPesanan.adapter =
                            RiwayatPesananAdapter(dataPesanan) { pesanan ->
                                showToast("Detail: ${pesanan.noPesanan}", Toast.LENGTH_SHORT)
                            }
                    } else {
                        rvRiwayatPesanan.adapter = RiwayatPesananAdapter(emptyList()) {}
                        showToast("Riwayat pesanan kosong", Toast.LENGTH_SHORT)
                    }
                }

                override fun onFailure(call: Call<RiwayatPesananResponse>, t: Throwable) {
                    if (call.isCanceled || !isActivitySafe()) return

                    rvRiwayatPesanan.adapter = RiwayatPesananAdapter(emptyList()) {}
                    showToast("Gagal mengambil riwayat pesanan: ${t.message}", Toast.LENGTH_LONG)
                }
            })
    }

    private fun loadCustomOrderFromDatabase() {
        val userId = session.getUserId()

        ApiClient.instance.getCustomOrder(userId)
            .enqueue(object : Callback<CustomOrderResponse> {
                override fun onResponse(
                    call: Call<CustomOrderResponse>,
                    response: Response<CustomOrderResponse>
                ) {
                    if (call.isCanceled || !isActivitySafe()) return

                    val body = response.body()
                    val data = body?.data.orEmpty()

                    if (response.isSuccessful && body?.status == true) {
                        val dataCustom = data.map { item ->
                            val customPaymentStatus = resolveCustomPaymentStatus(item)

                            CustomOrder(
                                id = item.id,
                                furnitureNama = item.furnitureNama ?: "-",
                                kayu = item.kayu ?: "-",
                                ukuran = item.ukuran ?: "-",
                                harga = item.harga ?: "Rp 0",
                                status = item.statusLabel ?: item.status ?: "-",
                                paymentStatus = customPaymentStatus,
                                imageUrl = item.gambarUrl ?: ""
                            )
                        }

                        rvCustomOrder.adapter =
                            CustomOrderAdapter(dataCustom) { order ->
                                bayarCustomOrder(order)
                            }
                    } else {
                        rvCustomOrder.adapter = CustomOrderAdapter(emptyList()) {}
                        showToast("Custom order kosong", Toast.LENGTH_SHORT)
                    }
                }

                override fun onFailure(call: Call<CustomOrderResponse>, t: Throwable) {
                    if (call.isCanceled || !isActivitySafe()) return

                    rvCustomOrder.adapter = CustomOrderAdapter(emptyList()) {}
                    showToast("Gagal mengambil custom order: ${t.message}", Toast.LENGTH_LONG)
                }
            })
    }

    private fun bayarCustomOrder(order: CustomOrder) {
        if (order.id <= 0) {
            showToast("ID custom order tidak valid", Toast.LENGTH_LONG)
            return
        }

        ApiClient.instance.bayarCustomOrder(order.id)
            .enqueue(object : Callback<PaymentResponse> {
                override fun onResponse(
                    call: Call<PaymentResponse>,
                    response: Response<PaymentResponse>
                ) {
                    if (call.isCanceled || !isActivitySafe()) return

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        val redirectUrl = body.redirect_url
                        val orderId = body.order_id

                        if (redirectUrl.isNullOrEmpty() || orderId.isNullOrEmpty()) {
                            showToast("Data pembayaran custom tidak lengkap", Toast.LENGTH_LONG)
                            return
                        }

                        pendingCustomPaymentOrderId = orderId

                        val intent = Intent(this@ProfileActivity, PaymentWebViewActivity::class.java)
                            .putExtra(PaymentWebViewActivity.EXTRA_PAYMENT_URL, redirectUrl)

                        customPaymentLauncher.launch(intent)
                    } else {
                        showToast(
                            body?.message
                                ?: "Gagal membuat pembayaran custom. Code: ${response.code()}",
                            Toast.LENGTH_LONG
                        )
                    }
                }

                override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                    if (call.isCanceled || !isActivitySafe()) return

                    showToast("Koneksi pembayaran custom gagal: ${t.message}", Toast.LENGTH_LONG)
                }
            })
    }

    private fun cekStatusPembayaranCustom(orderId: String, percobaan: Int = 1) {
        ApiClient.instance.cekStatusPembayaran(orderId)
            .enqueue(object : Callback<PaymentStatusResponse> {
                override fun onResponse(
                    call: Call<PaymentStatusResponse>,
                    response: Response<PaymentStatusResponse>
                ) {
                    if (call.isCanceled || !isActivitySafe()) return

                    when (response.body()?.payment_status.orEmpty().lowercase()) {
                        "paid", "settlement", "capture", "success" -> {
                            pendingCustomPaymentOrderId = null
                            customPaymentStatusHandler.removeCallbacksAndMessages(null)
                            showToast("Pembayaran custom berhasil", Toast.LENGTH_LONG)
                            loadCustomOrderFromDatabase()
                            loadRiwayatPesananFromDatabase()
                        }

                        "deny", "expire", "cancel", "failed", "failure" -> {
                            pendingCustomPaymentOrderId = null
                            customPaymentStatusHandler.removeCallbacksAndMessages(null)
                            showToast("Pembayaran custom gagal atau dibatalkan", Toast.LENGTH_LONG)
                            loadCustomOrderFromDatabase()
                            loadRiwayatPesananFromDatabase()
                        }

                        else -> {
                            if (percobaan < MAX_CUSTOM_PAYMENT_STATUS_RETRY) {
                                jadwalkanCekStatusCustomUlang(orderId, percobaan)
                            } else {
                                showToast("Pembayaran custom masih pending", Toast.LENGTH_LONG)
                                loadCustomOrderFromDatabase()
                                loadRiwayatPesananFromDatabase()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PaymentStatusResponse>, t: Throwable) {
                    if (call.isCanceled || !isActivitySafe()) return

                    if (percobaan < MAX_CUSTOM_PAYMENT_STATUS_RETRY) {
                        jadwalkanCekStatusCustomUlang(orderId, percobaan)
                    } else {
                        showToast(
                            "Gagal cek status pembayaran custom: ${t.message}",
                            Toast.LENGTH_LONG
                        )
                        loadCustomOrderFromDatabase()
                        loadRiwayatPesananFromDatabase()
                    }
                }
            })
    }

    private fun jadwalkanCekStatusCustomUlang(orderId: String, percobaan: Int) {
        customPaymentStatusHandler.postDelayed({
            if (isActivitySafe()) {
                cekStatusPembayaranCustom(orderId, percobaan + 1)
            }
        }, CUSTOM_PAYMENT_STATUS_RETRY_DELAY_MS)
    }

    private fun resolveCustomPaymentStatus(
        item: prasetya.daffa.proyek_uas.api.CustomOrderApiItem
    ): String {
        val paymentStatus = item.paymentStatus.orEmpty()
        val paymentLabel = item.paymentLabel.orEmpty()
        val metode = item.metodePembayaran.orEmpty()

        if (item.isPaid == true || !item.paidAt.isNullOrEmpty()) {
            return "paid"
        }

        if (paymentStatus.isNotBlank()) return paymentStatus
        if (paymentLabel.isNotBlank()) return paymentLabel

        if (metode.isNotBlank() && item.status.orEmpty().equals("selesai", ignoreCase = true)) {
            return "paid"
        }

        return "-"
    }

    private fun showSection(index: Int) {
        allSections.forEach { section ->
            section.visibility = android.view.View.GONE
        }

        allSections[index].visibility = android.view.View.VISIBLE

        allNavItems.forEach { nav ->
            nav.setBackgroundResource(R.drawable.bg_nav_item_inactive)

            (nav.getChildAt(0) as? ImageView)?.imageTintList =
                ColorStateList.valueOf(Color.parseColor("#555555"))

            (nav.getChildAt(1) as? TextView)?.apply {
                setTextColor(Color.parseColor("#555555"))
                setTypeface(null, Typeface.NORMAL)
            }
        }

        allNavItems[index].apply {
            setBackgroundResource(R.drawable.bg_nav_item_active)

            (getChildAt(0) as? ImageView)?.imageTintList =
                ColorStateList.valueOf(Color.WHITE)

            (getChildAt(1) as? TextView)?.apply {
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun formatMetodePembayaran(value: String?): String {
        return when (value.orEmpty().lowercase()) {
            "midtrans" -> "Midtrans"
            "transfer_bank" -> "Transfer Bank"
            "cod" -> "COD"
            "" -> "-"
            else -> value.orEmpty()
                .replace("_", " ")
                .replaceFirstChar { it.uppercase() }
        }
    }

    override fun onDestroy() {
        customPaymentStatusHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val MAX_CUSTOM_PAYMENT_STATUS_RETRY = 4
        private const val CUSTOM_PAYMENT_STATUS_RETRY_DELAY_MS = 1_200L
    }
}