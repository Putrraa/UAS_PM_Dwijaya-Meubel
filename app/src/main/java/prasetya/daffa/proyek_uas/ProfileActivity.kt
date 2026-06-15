package prasetya.daffa.proyek_uas

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import prasetya.daffa.proyek_uas.api.ProfileResponse
import prasetya.daffa.proyek_uas.api.RiwayatPesananResponse
import prasetya.daffa.proyek_uas.databinding.ActivityProfileBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity(), View.OnClickListener {

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
    private lateinit var navKeamananAkun: LinearLayout

    private lateinit var sectionProfilSaya: LinearLayout
    private lateinit var sectionRiwayatPesanan: LinearLayout
    private lateinit var sectionCustomOrder: LinearLayout
    private lateinit var sectionKeamananAkun: LinearLayout

    private lateinit var rvRiwayatPesanan: RecyclerView
    private lateinit var rvCustomOrder: RecyclerView

    private var isPasswordLamaVisible = false
    private var isPasswordBaruVisible = false
    private var isKonfirmasiVisible   = false

    private val allNavItems: List<LinearLayout>
        get() = listOf(navProfilSaya, navRiwayatPesanan, navCustomOrder, navKeamananAkun)

    private val allSections: List<LinearLayout>
        get() = listOf(sectionProfilSaya, sectionRiwayatPesanan, sectionCustomOrder, sectionKeamananAkun)

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
        setupPasswordToggle()
        setupPasswordWarning()

        showSection(0)

        loadProfileFromDatabase()
        loadRiwayatPesananFromDatabase()
        loadCustomOrderFromDatabase()
    }

    private fun bindViews() {
        btnBack           = findViewById(R.id.btnBack)
        tvAvatarInitial   = findViewById(R.id.tvAvatarInitial)
        tvProfileName     = findViewById(R.id.tvProfileName)
        tvProfileEmail    = findViewById(R.id.tvProfileEmail)
        etNamaLengkap     = findViewById(R.id.etNamaLengkap)
        etEmail           = findViewById(R.id.etEmail)
        navProfilSaya     = findViewById(R.id.navProfilSaya)
        navRiwayatPesanan = findViewById(R.id.navRiwayatPesanan)
        navCustomOrder    = findViewById(R.id.navCustomOrder)
        navKeamananAkun   = findViewById(R.id.navKeamananAkun)
        sectionProfilSaya     = findViewById(R.id.sectionProfilSaya)
        sectionRiwayatPesanan = findViewById(R.id.sectionRiwayatPesanan)
        sectionCustomOrder    = findViewById(R.id.sectionCustomOrder)
        sectionKeamananAkun   = findViewById(R.id.sectionKeamananAkun)
        rvRiwayatPesanan  = findViewById(R.id.rvRiwayatPesanan)
        rvCustomOrder     = findViewById(R.id.rvCustomOrder)
    }

    private fun setupRecyclerView() {
        rvRiwayatPesanan.layoutManager = LinearLayoutManager(this)
        rvCustomOrder.layoutManager    = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener           { finish() }
        navProfilSaya.setOnClickListener     { showSection(0) }
        navRiwayatPesanan.setOnClickListener { showSection(1) }
        navCustomOrder.setOnClickListener    { showSection(2) }
        navKeamananAkun.setOnClickListener   { showSection(3) }

        findViewById<Button>(R.id.btnUpdatePassword).setOnClickListener {
            updatePassword()
        }
    }

    private fun setupPasswordToggle() {
        val etPasswordLama     = findViewById<EditText>(R.id.etPasswordLama)
        val etPasswordBaru     = findViewById<EditText>(R.id.etPasswordBaru)
        val etKonfirmasi       = findViewById<EditText>(R.id.etKonfirmasiPassword)
        val ivToggleLama       = findViewById<ImageView>(R.id.ivTogglePasswordLama)
        val ivToggleBaru       = findViewById<ImageView>(R.id.ivTogglePasswordBaru)
        val ivToggleKonfirmasi = findViewById<ImageView>(R.id.ivToggleKonfirmasi)

        ivToggleLama.setOnClickListener {
            togglePassword(etPasswordLama, ivToggleLama, ::isPasswordLamaVisible) {
                isPasswordLamaVisible = it
            }
        }
        ivToggleBaru.setOnClickListener {
            togglePassword(etPasswordBaru, ivToggleBaru, ::isPasswordBaruVisible) {
                isPasswordBaruVisible = it
            }
        }
        ivToggleKonfirmasi.setOnClickListener {
            togglePassword(etKonfirmasi, ivToggleKonfirmasi, ::isKonfirmasiVisible) {
                isKonfirmasiVisible = it
            }
        }
    }

    private fun togglePassword(
        editText: EditText,
        icon: ImageView,
        isVisible: () -> Boolean,
        setVisible: (Boolean) -> Unit
    ) {
        val newVisible = !isVisible()
        val animOut = AnimationUtils.loadAnimation(this, R.anim.anim_eye_close)
        val animIn  = AnimationUtils.loadAnimation(this, R.anim.anim_eye_open)

        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(a: Animation?) {}
            override fun onAnimationRepeat(a: Animation?) {}
            override fun onAnimationEnd(a: Animation?) {
                icon.setImageResource(
                    if (newVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                )
                icon.alpha = if (newVisible) 0.9f else 0.45f
                icon.startAnimation(animIn)
            }
        })

        icon.startAnimation(animOut)
        setVisible(newVisible)

        val cursor = editText.selectionEnd
        editText.transformationMethod = if (newVisible)
            HideReturnsTransformationMethod.getInstance()
        else
            PasswordTransformationMethod.getInstance()
        editText.setSelection(cursor)
    }

    private fun setupPasswordWarning() {
        val etPasswordBaru = findViewById<EditText>(R.id.etPasswordBaru)
        val etKonfirmasi   = findViewById<EditText>(R.id.etKonfirmasiPassword)
        val tvWarning      = findViewById<TextView>(R.id.tvPasswordWarning)

        etKonfirmasi.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch(etPasswordBaru, etKonfirmasi, tvWarning)
            }
        })

        etPasswordBaru.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (etKonfirmasi.text.isNotEmpty()) {
                    checkPasswordMatch(etPasswordBaru, etKonfirmasi, tvWarning)
                }
            }
        })
    }

    private fun checkPasswordMatch(
        etBaru: EditText,
        etKonfirmasi: EditText,
        tvWarning: TextView
    ) {
        val baru    = etBaru.text.toString()
        val konfirm = etKonfirmasi.text.toString()

        when {
            konfirm.isEmpty() -> {
                tvWarning.visibility = View.GONE
            }
            baru != konfirm -> {
                tvWarning.text = "⚠ Password tidak cocok"
                tvWarning.setTextColor(0xFFE53935.toInt())
                tvWarning.visibility = View.VISIBLE
            }
            else -> {
                tvWarning.text = "✓ Password cocok"
                tvWarning.setTextColor(0xFF3D6148.toInt())
                tvWarning.visibility = View.VISIBLE
            }
        }
    }

    private fun updatePassword() {
        val etPasswordLama = findViewById<EditText>(R.id.etPasswordLama)
        val etPasswordBaru = findViewById<EditText>(R.id.etPasswordBaru)
        val etKonfirmasi   = findViewById<EditText>(R.id.etKonfirmasiPassword)
        val tvWarning      = findViewById<TextView>(R.id.tvPasswordWarning)
        val btnUpdate      = findViewById<Button>(R.id.btnUpdatePassword)

        val passwordLama = etPasswordLama.text.toString().trim()
        val passwordBaru = etPasswordBaru.text.toString().trim()
        val konfirmasi   = etKonfirmasi.text.toString().trim()

        if (passwordLama.isEmpty()) {
            etPasswordLama.error = "Password lama wajib diisi"
            etPasswordLama.requestFocus()
            return
        }
        if (passwordBaru.isEmpty()) {
            etPasswordBaru.error = "Password baru wajib diisi"
            etPasswordBaru.requestFocus()
            return
        }
        if (passwordBaru.length < 6) {
            etPasswordBaru.error = "Password minimal 6 karakter"
            etPasswordBaru.requestFocus()
            return
        }
        if (konfirmasi.isEmpty()) {
            etKonfirmasi.error = "Konfirmasi password wajib diisi"
            etKonfirmasi.requestFocus()
            return
        }
        if (passwordBaru != konfirmasi) {
            tvWarning.text = "⚠ Password tidak cocok"
            tvWarning.setTextColor(0xFFE53935.toInt())
            tvWarning.visibility = View.VISIBLE
            etKonfirmasi.requestFocus()
            return
        }

        btnUpdate.isEnabled = false
        btnUpdate.text      = "Memproses..."

        // Panggil API update password di sini jika tersedia
        // Sementara dummy success:
        btnUpdate.isEnabled = true
        btnUpdate.text      = "Update Password"

        Toast.makeText(this, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()

        etPasswordLama.text.clear()
        etPasswordBaru.text.clear()
        etKonfirmasi.text.clear()
        tvWarning.visibility = View.GONE

        // Reset icon eye
        isPasswordLamaVisible = false
        isPasswordBaruVisible = false
        isKonfirmasiVisible   = false
        findViewById<ImageView>(R.id.ivTogglePasswordLama).apply {
            setImageResource(R.drawable.ic_eye_closed); alpha = 0.45f
        }
        findViewById<ImageView>(R.id.ivTogglePasswordBaru).apply {
            setImageResource(R.drawable.ic_eye_closed); alpha = 0.45f
        }
        findViewById<ImageView>(R.id.ivToggleKonfirmasi).apply {
            setImageResource(R.drawable.ic_eye_closed); alpha = 0.45f
        }
    }

    override fun onClick(v: View?) {}

    private fun isActivitySafe() = !isFinishing && !isDestroyed

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (!isActivitySafe()) return
        Toast.makeText(this, message, duration).show()
    }

    private fun loadProfileFromDatabase() {
        val userId = session.getUserId()
        ApiClient.instance.getProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (call.isCanceled || !isActivitySafe()) return
                val body = response.body()
                if (response.isSuccessful && body?.status == true && body.data != null) {
                    val user  = body.data
                    val nama  = user.name  ?: "Customer"
                    val email = user.email ?: "customer@gmail.com"
                    tvProfileName.text  = nama
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
        val nama  = session.getName().ifEmpty  { "Customer" }
        val email = session.getEmail().ifEmpty { "customer@gmail.com" }
        tvProfileName.text  = nama
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
                                tanggal   = item.tanggal   ?: "-",
                                total     = item.total     ?: "Rp 0",
                                status    = item.status    ?: "-"
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
                            CustomOrder(
                                furnitureNama = item.furnitureNama ?: "-",
                                kayu          = item.kayu          ?: "-",
                                ukuran        = item.ukuran        ?: "-",
                                harga         = item.harga         ?: "Rp 0",
                                status        = item.status        ?: "-",
                                imageUrl      = item.gambarUrl     ?: ""
                            )
                        }
                        rvCustomOrder.adapter =
                            CustomOrderAdapter(dataCustom) { order ->
                                showToast("Custom Order: ${order.furnitureNama}", Toast.LENGTH_SHORT)
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

    private fun showSection(index: Int) {
        allSections.forEach { it.visibility = View.GONE }
        allSections[index].visibility = View.VISIBLE

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
}