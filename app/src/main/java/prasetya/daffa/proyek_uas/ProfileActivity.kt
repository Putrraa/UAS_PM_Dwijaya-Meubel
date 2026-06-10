package prasetya.daffa.proyek_uas

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import prasetya.daffa.proyek_uas.databinding.ActivityProfileBinding


class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var b: ActivityProfileBinding

    // Sidebar nav items
    private lateinit var navProfilSaya: LinearLayout
    private lateinit var navRiwayatPesanan: LinearLayout
    private lateinit var navCustomOrder: LinearLayout
    private lateinit var navKeamananAkun: LinearLayout

    // Content sections
    private lateinit var sectionProfilSaya: LinearLayout
    private lateinit var sectionRiwayatPesanan: LinearLayout
    private lateinit var sectionCustomOrder: LinearLayout
    private lateinit var sectionKeamananAkun: LinearLayout

    // RecyclerViews
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

        // Biar status bar tidak ungu dan layar tidak kepotong
        window.statusBarColor = Color.parseColor("#F5F5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        // Bind views
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

        setupRiwayatPesanan()
        setupCustomOrder()

        navProfilSaya.setOnClickListener { showSection(0) }
        navRiwayatPesanan.setOnClickListener { showSection(1) }
        navCustomOrder.setOnClickListener { showSection(2) }
        navKeamananAkun.setOnClickListener { showSection(3) }

        showSection(0)

        findViewById<Button>(R.id.btnUpdatePassword).setOnClickListener {
            val passwordLama = findViewById<EditText>(R.id.etPasswordLama).text.toString()
            val passwordBaru = findViewById<EditText>(R.id.etPasswordBaru).text.toString()
            val konfirmasi = findViewById<EditText>(R.id.etKonfirmasiPassword).text.toString()

            if (passwordLama.isEmpty() || passwordBaru.isEmpty() || konfirmasi.isEmpty()) {
                Toast.makeText(this, "Semua field password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordBaru != konfirmasi) {
                Toast.makeText(this, "Password baru tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordBaru.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }
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

    private fun setupRiwayatPesanan() {
        val dummyData = listOf(
            RiwayatPesanan("#DWJ-3", "01 Jun 2026", "Rp 4.000.000", "Diproses"),
            RiwayatPesanan("#DWJ-2", "01 Jun 2026", "Rp 2.000.000", "Diproses"),
            RiwayatPesanan("#DWJ-1", "01 Jun 2026", "Rp 2.000.000", "Diproses")
        )

        val adapter = RiwayatPesananAdapter(dummyData) { pesanan ->
            Toast.makeText(this, "Detail: ${pesanan.noPesanan}", Toast.LENGTH_SHORT).show()
        }

        rvRiwayatPesanan.layoutManager = LinearLayoutManager(this)
        rvRiwayatPesanan.adapter = adapter
    }

    private fun setupCustomOrder() {
        val dummyData = listOf(
            CustomOrder(
                furnitureNama = "meja",
                kayu = "jati",
                ukuran = "120x120x50",
                harga = "Rp 200.000.000",
                status = "Pending"
            )
        )

        val adapter = CustomOrderAdapter(dummyData) { order ->
            Toast.makeText(this, "Bayar: ${order.furnitureNama}", Toast.LENGTH_SHORT).show()
        }

        rvCustomOrder.layoutManager = LinearLayoutManager(this)
        rvCustomOrder.adapter = adapter
    }
}