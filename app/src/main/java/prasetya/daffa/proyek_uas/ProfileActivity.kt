package prasetya.daffa.proyek_uas

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
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

    private val allNavItems: List<LinearLayout>
        get() = listOf(
            navProfilSaya,
            navRiwayatPesanan,
            navCustomOrder,
            navKeamananAkun
        )

    private val allSections: List<LinearLayout>
        get() = listOf(
            sectionProfilSaya,
            sectionRiwayatPesanan,
            sectionCustomOrder,
            sectionKeamananAkun
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
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        bindViews()
        setupRecyclerView()
        setupClickListeners()

        showSection(0)

        loadProfileFromDatabase()
        loadRiwayatPesananFromDatabase()
        loadCustomOrderFromDatabase()
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
        navKeamananAkun = findViewById(R.id.navKeamananAkun)

        sectionProfilSaya = findViewById(R.id.sectionProfilSaya)
        sectionRiwayatPesanan = findViewById(R.id.sectionRiwayatPesanan)
        sectionCustomOrder = findViewById(R.id.sectionCustomOrder)
        sectionKeamananAkun = findViewById(R.id.sectionKeamananAkun)

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

        navKeamananAkun.setOnClickListener {
            showSection(3)
        }

        findViewById<Button>(R.id.btnUpdatePassword).setOnClickListener {
            updatePasswordDummy()
        }
    }

    private fun loadProfileFromDatabase() {
        val userId = session.getUserId()

        ApiClient.instance.getProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                val body = response.body()

                if (response.isSuccessful && body?.status == true && body.data != null) {
                    val user = body.data

                    tvProfileName.text = user.name
                    tvProfileEmail.text = user.email
                    etNamaLengkap.setText(user.name)
                    etEmail.setText(user.email)

                    val initial = user.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"
                    tvAvatarInitial.text = initial
                } else {
                    tampilkanDataSession()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                tampilkanDataSession()
                Toast.makeText(
                    this@ProfileActivity,
                    "Gagal mengambil profile: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun tampilkanDataSession() {
        val nama = session.getName()
        val email = session.getEmail()

        val namaTampil = if (nama.isNotEmpty()) nama else "Customer"
        val emailTampil = if (email.isNotEmpty()) email else "customer@gmail.com"

        tvProfileName.text = namaTampil
        tvProfileEmail.text = emailTampil
        etNamaLengkap.setText(namaTampil)
        etEmail.setText(emailTampil)

        val initial = namaTampil.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        tvAvatarInitial.text = initial
    }

    private fun loadRiwayatPesananFromDatabase() {
        val userId = session.getUserId()

        ApiClient.instance.getRiwayatPesanan(userId)
            .enqueue(object : Callback<RiwayatPesananResponse> {
                override fun onResponse(
                    call: Call<RiwayatPesananResponse>,
                    response: Response<RiwayatPesananResponse>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        val dataPesanan = body.data.map { item ->
                            RiwayatPesanan(
                                noPesanan = item.noPesanan,
                                tanggal = item.tanggal,
                                total = item.total,
                                status = item.status
                            )
                        }

                        val adapter = RiwayatPesananAdapter(dataPesanan) { pesanan ->
                            Toast.makeText(
                                this@ProfileActivity,
                                "Detail: ${pesanan.noPesanan}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        rvRiwayatPesanan.adapter = adapter
                    } else {
                        rvRiwayatPesanan.adapter = RiwayatPesananAdapter(emptyList()) {}
                        Toast.makeText(
                            this@ProfileActivity,
                            "Riwayat pesanan kosong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<RiwayatPesananResponse>, t: Throwable) {
                    rvRiwayatPesanan.adapter = RiwayatPesananAdapter(emptyList()) {}

                    Toast.makeText(
                        this@ProfileActivity,
                        "Gagal mengambil riwayat pesanan: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
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
                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        val dataCustom = body.data.map { item ->
                            CustomOrder(
                                furnitureNama = item.furnitureNama,
                                kayu = item.kayu,
                                ukuran = item.ukuran,
                                harga = item.harga,
                                status = item.status,
                                imageUrl = item.gambarUrl ?: ""
                            )
                        }

                        val adapter = CustomOrderAdapter(dataCustom) { order ->
                            Toast.makeText(
                                this@ProfileActivity,
                                "Custom Order: ${order.furnitureNama}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        rvCustomOrder.adapter = adapter
                    } else {
                        rvCustomOrder.adapter = CustomOrderAdapter(emptyList()) {}
                        Toast.makeText(
                            this@ProfileActivity,
                            "Custom order kosong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CustomOrderResponse>, t: Throwable) {
                    rvCustomOrder.adapter = CustomOrderAdapter(emptyList()) {}

                    Toast.makeText(
                        this@ProfileActivity,
                        "Gagal mengambil custom order: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun updatePasswordDummy() {
        val passwordLama = findViewById<EditText>(R.id.etPasswordLama).text.toString().trim()
        val passwordBaru = findViewById<EditText>(R.id.etPasswordBaru).text.toString().trim()
        val konfirmasi = findViewById<EditText>(R.id.etKonfirmasiPassword).text.toString().trim()

        if (passwordLama.isEmpty() || passwordBaru.isEmpty() || konfirmasi.isEmpty()) {
            Toast.makeText(this, "Semua field password harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordBaru != konfirmasi) {
            Toast.makeText(this, "Password baru tidak cocok!", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordBaru.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        // Belum dipakai
    }

    private fun showSection(index: Int) {
        allSections.forEach {
            it.visibility = View.GONE
        }

        allSections[index].visibility = View.VISIBLE

        allNavItems.forEach { nav ->
            nav.setBackgroundResource(R.drawable.bg_nav_item_inactive)

            val icon = nav.getChildAt(0) as? ImageView
            val text = nav.getChildAt(1) as? TextView

            icon?.imageTintList = ColorStateList.valueOf(Color.parseColor("#555555"))

            text?.apply {
                setTextColor(Color.parseColor("#555555"))
                setTypeface(null, Typeface.NORMAL)
            }
        }

        allNavItems[index].apply {
            setBackgroundResource(R.drawable.bg_nav_item_active)

            val icon = getChildAt(0) as? ImageView
            val text = getChildAt(1) as? TextView

            icon?.imageTintList = ColorStateList.valueOf(Color.WHITE)

            text?.apply {
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
            }
        }
    }
}