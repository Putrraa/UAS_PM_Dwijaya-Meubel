package prasetya.daffa.proyek_uas.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.adapter.CustomOrder
import prasetya.daffa.proyek_uas.adapter.CustomOrderAdapter
import prasetya.daffa.proyek_uas.adapter.RiwayatPesanan
import prasetya.daffa.proyek_uas.adapter.RiwayatPesananAdapter

class ProfileFragment : Fragment() {

    // ── Sidebar nav items ──
    private lateinit var navProfilSaya: LinearLayout
    private lateinit var navRiwayatPesanan: LinearLayout
    private lateinit var navCustomOrder: LinearLayout
    private lateinit var navKeamananAkun: LinearLayout

    // ── Content sections ──
    private lateinit var sectionProfilSaya: LinearLayout
    private lateinit var sectionRiwayatPesanan: LinearLayout
    private lateinit var sectionCustomOrder: LinearLayout
    private lateinit var sectionKeamananAkun: LinearLayout

    // ── RecyclerViews ──
    private lateinit var rvRiwayatPesanan: RecyclerView
    private lateinit var rvCustomOrder: RecyclerView

    // ── Semua nav items dalam list agar mudah di-reset ──
    private val allNavItems get() = listOf(
        navProfilSaya, navRiwayatPesanan, navCustomOrder, navKeamananAkun
    )
    private val allSections get() = listOf(
        sectionProfilSaya, sectionRiwayatPesanan, sectionCustomOrder, sectionKeamananAkun
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Bind views ──
        navProfilSaya       = view.findViewById(R.id.navProfilSaya)
        navRiwayatPesanan   = view.findViewById(R.id.navRiwayatPesanan)
        navCustomOrder      = view.findViewById(R.id.navCustomOrder)
        navKeamananAkun     = view.findViewById(R.id.navKeamananAkun)

        sectionProfilSaya       = view.findViewById(R.id.sectionProfilSaya)
        sectionRiwayatPesanan   = view.findViewById(R.id.sectionRiwayatPesanan)
        sectionCustomOrder      = view.findViewById(R.id.sectionCustomOrder)
        sectionKeamananAkun     = view.findViewById(R.id.sectionKeamananAkun)

        rvRiwayatPesanan = view.findViewById(R.id.rvRiwayatPesanan)
        rvCustomOrder    = view.findViewById(R.id.rvCustomOrder)

        // ── Setup RecyclerViews ──
        setupRiwayatPesanan()
        setupCustomOrder()

        // ── Sidebar nav click listeners ──
        navProfilSaya.setOnClickListener     { showSection(0) }
        navRiwayatPesanan.setOnClickListener { showSection(1) }
        navCustomOrder.setOnClickListener    { showSection(2) }
        navKeamananAkun.setOnClickListener   { showSection(3) }

        // ── Tampilkan section pertama saat mulai ──
        showSection(0)

        // ── Tombol Update Password ──
        view.findViewById<Button>(R.id.btnUpdatePassword).setOnClickListener {
            val passwordLama    = view.findViewById<EditText>(R.id.etPasswordLama).text.toString()
            val passwordBaru    = view.findViewById<EditText>(R.id.etPasswordBaru).text.toString()
            val konfirmasi      = view.findViewById<EditText>(R.id.etKonfirmasiPassword).text.toString()

            if (passwordBaru != konfirmasi) {
                Toast.makeText(requireContext(), "Password baru tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (passwordBaru.length < 6) {
                Toast.makeText(requireContext(), "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // TODO: kirim ke API Laravel
            Toast.makeText(requireContext(), "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    // ─────────────────────────────────────────────
    //  Show/hide section + aktifkan nav yang dipilih
    // ─────────────────────────────────────────────
    private fun showSection(index: Int) {
        // Sembunyikan semua section
        allSections.forEach { it.visibility = View.GONE }
        // Tampilkan section yang dipilih
        allSections[index].visibility = View.VISIBLE

        // Reset semua nav ke inactive
        allNavItems.forEach { nav ->
            nav.setBackgroundResource(R.drawable.bg_nav_item_inactive)
            // Ubah warna teks dan icon ke abu-abu
            (nav.getChildAt(0) as? ImageView)?.imageTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#555555")
                )
            (nav.getChildAt(1) as? TextView)?.apply {
                setTextColor(android.graphics.Color.parseColor("#555555"))
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }

        // Set nav yang aktif
        allNavItems[index].apply {
            setBackgroundResource(R.drawable.bg_nav_item_active)
            (getChildAt(0) as? ImageView)?.imageTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.WHITE
                )
            (getChildAt(1) as? TextView)?.apply {
                setTextColor(android.graphics.Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }

    // ─────────────────────────────────────────────
    //  Setup RecyclerView Riwayat Pesanan
    // ─────────────────────────────────────────────
    private fun setupRiwayatPesanan() {
        // Data dummy — ganti dengan data dari API
        val dummyData = listOf(
            RiwayatPesanan("#DWJ-3", "01 Jun 2026", "Rp 4.000.000", "Diproses"),
            RiwayatPesanan("#DWJ-2", "01 Jun 2026", "Rp 2.000.000", "Diproses"),
            RiwayatPesanan("#DWJ-1", "01 Jun 2026", "Rp 2.000.000", "Diproses")
        )

        val adapter = RiwayatPesananAdapter(dummyData) { pesanan ->
            // Handle klik tombol Detail
            Toast.makeText(requireContext(), "Detail: ${pesanan.noPesanan}", Toast.LENGTH_SHORT).show()
            // TODO: navigate ke halaman detail pesanan
        }

        rvRiwayatPesanan.layoutManager = LinearLayoutManager(requireContext())
        rvRiwayatPesanan.adapter = adapter
    }

    // ─────────────────────────────────────────────
    //  Setup RecyclerView Custom Order
    // ─────────────────────────────────────────────
    private fun setupCustomOrder() {
        // Data dummy — ganti dengan data dari API
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
            // Handle klik tombol Bayar Sekarang
            Toast.makeText(requireContext(), "Bayar: ${order.furnitureNama}", Toast.LENGTH_SHORT).show()
            // TODO: navigate ke halaman pembayaran
        }

        rvCustomOrder.layoutManager = LinearLayoutManager(requireContext())
        rvCustomOrder.adapter = adapter
    }
}
