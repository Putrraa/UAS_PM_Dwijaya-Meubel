package prasetya.daffa.proyek_uas

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.adapter.KategoriChipAdapter
import prasetya.daffa.proyek_uas.adapter.KategoriChipItem
import prasetya.daffa.proyek_uas.adapter.ProdukKategoriAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.api.BarangListResponse
import prasetya.daffa.proyek_uas.api.KategoriResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.DialogDetailProdukBinding
import prasetya.daffa.proyek_uas.databinding.KategoriProdukFragmentBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class KategoriProdukFragment : Fragment() {

    private var _b: KategoriProdukFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var session: SessionManager

    private lateinit var kategoriAdapter: KategoriChipAdapter
    private lateinit var produkAdapter: ProdukKategoriAdapter

    private val listKategori = mutableListOf<KategoriChipItem>()
    private val listProdukSemua = mutableListOf<Barang>()
    private val listProdukTampil = mutableListOf<Barang>()

    private var selectedSlug = "semua"
    private var selectedNamaKategori = "Semua Produk"

    private val sortList = listOf(
        "Default sorting",
        "Sort by latest",
        "Harga Terendah",
        "Harga Tertinggi",
        "Nama A-Z",
        "Nama Z-A"
    )

    companion object {
        fun newInstance(slug: String): KategoriProdukFragment {
            return KategoriProdukFragment().apply {
                arguments = bundleOf("slug" to slug)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedSlug = arguments?.getString("slug") ?: "semua"

        if (selectedSlug.isBlank()) {
            selectedSlug = "semua"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = KategoriProdukFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())

        setupButton()
        setupKategori()
        setupProduk()
        setupSort()
        setupSearch()

        loadKategori()
        loadProduk()
    }

    private fun setupButton() {

        b.btnSearchProduk.setOnClickListener {
            applyFilterSort()
        }
    }

    private fun setupKategori() {
        kategoriAdapter = KategoriChipAdapter(listKategori) { kategori ->
            selectedSlug = kategori.slug
            selectedNamaKategori = kategori.nama
            updateHeader()
            applyFilterSort()
        }

        b.rvKategoriProduk.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        b.rvKategoriProduk.adapter = kategoriAdapter
    }

    private fun setupProduk() {
        produkAdapter = ProdukKategoriAdapter(listProdukTampil) { barang ->
            showDialogDetailProduk(barang)
        }

        b.rvProdukKategori.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rvProdukKategori.isNestedScrollingEnabled = false
        b.rvProdukKategori.setHasFixedSize(false)
        b.rvProdukKategori.adapter = produkAdapter
    }

    private fun setupSort() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortList
        )

        b.spinnerSortProduk.adapter = adapter

        b.spinnerSortProduk.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    applyFilterSort()
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        )
    }

    private fun setupSearch() {
        b.etSearchProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterSort()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadKategori() {
        ApiClient.instance.getKategori().enqueue(object : Callback<KategoriResponse> {
            override fun onResponse(
                call: Call<KategoriResponse>,
                response: Response<KategoriResponse>
            ) {
                if (call.isCanceled || !isViewSafe()) return

                val body = response.body()
                val data = body?.data.orEmpty()

                if (response.isSuccessful && body?.status == true) {
                    val chips = mutableListOf<KategoriChipItem>()
                    chips.add(KategoriChipItem("Semua", "semua"))

                    data.forEach { kategori ->
                        val nama = kategori.nama_kategori.orEmpty()

                        if (nama.isBlank()) return@forEach

                        chips.add(
                            KategoriChipItem(
                                nama = nama,
                                slug = nama.toSlug()
                            )
                        )
                    }

                    val selected = if (chips.any { it.slug == selectedSlug }) {
                        selectedSlug
                    } else {
                        "semua"
                    }

                    selectedSlug = selected
                    selectedNamaKategori = chips.find { it.slug == selectedSlug }?.nama ?: "Semua"

                    kategoriAdapter.setData(chips, selectedSlug)
                    updateHeader()
                    applyFilterSort()
                } else {
                    showToast(
                        body?.message ?: "Gagal mengambil kategori",
                        Toast.LENGTH_SHORT
                    )
                }
            }

            override fun onFailure(call: Call<KategoriResponse>, t: Throwable) {
                if (call.isCanceled || !isViewSafe()) return

                showToast(
                    "Koneksi kategori gagal: ${t.message}",
                    Toast.LENGTH_LONG
                )
            }
        })
    }

    private fun loadProduk() {
        showLoading(true)

        ApiClient.instance.getBarang().enqueue(object : Callback<BarangListResponse> {
            override fun onResponse(
                call: Call<BarangListResponse>,
                response: Response<BarangListResponse>
            ) {
                if (call.isCanceled || !isViewSafe()) return

                showLoading(false)

                val body = response.body()
                val data = body?.data.orEmpty()

                if (response.isSuccessful && body?.status == true) {
                    listProdukSemua.clear()
                    listProdukSemua.addAll(data)

                    android.util.Log.d("CEK_KATEGORI_PRODUK", "Total API: ${data.size}")

                    applyFilterSort()
                } else {
                    updateEmptyState(emptyList())

                    showToast(
                        body?.message ?: "Gagal mengambil produk",
                        Toast.LENGTH_SHORT
                    )
                }
            }

            override fun onFailure(call: Call<BarangListResponse>, t: Throwable) {
                if (call.isCanceled || !isViewSafe()) return

                showLoading(false)
                updateEmptyState(emptyList())

                showToast(
                    "Koneksi produk gagal: ${t.message}",
                    Toast.LENGTH_LONG
                )
            }
        })
    }

    private fun applyFilterSort() {
        if (!isViewSafe() || !::produkAdapter.isInitialized) return

        val keyword = b.etSearchProduk.text.toString().trim().lowercase()
        val sortPosition = b.spinnerSortProduk.selectedItemPosition

        var result = listProdukSemua.toList()

        if (selectedSlug != "semua") {
            result = result.filter { barang ->
                val kategoriSlug = barang.kategori?.nama_kategori.orEmpty().toSlug()
                kategoriSlug == selectedSlug
            }
        }

        if (keyword.isNotEmpty()) {
            result = result.filter { barang ->
                val nama = barang.nama_barang.orEmpty().lowercase()
                val kategori = barang.kategori?.nama_kategori.orEmpty().lowercase()
                val bahan = barang.bahan?.nama_bahan.orEmpty().lowercase()

                nama.contains(keyword) ||
                        kategori.contains(keyword) ||
                        bahan.contains(keyword)
            }
        }

        result = when (sortPosition) {
            1 -> result.sortedByDescending { it.id ?: 0 }
            2 -> result.sortedBy { parseHarga(it.harga) }
            3 -> result.sortedByDescending { parseHarga(it.harga) }
            4 -> result.sortedBy { it.nama_barang.orEmpty() }
            5 -> result.sortedByDescending { it.nama_barang.orEmpty() }
            else -> result
        }

        produkAdapter.setData(result)

        android.util.Log.d(
            "CEK_KATEGORI_PRODUK",
            "Total tampil: ${result.size}, selectedSlug: $selectedSlug, keyword: $keyword"
        )

        updateEmptyState(result)
        b.tvResultInfo.text = "Showing all ${result.size} results"
    }

    private fun updateHeader() {
        if (!isViewSafe()) return

        val title = if (selectedSlug == "semua") {
            "Semua Produk"
        } else {
            selectedNamaKategori
        }

        b.tvJudulKategori.text = title
        b.tvSubtitleKategori.text = "$title – Toko Furniture Kami"
    }

    private fun updateEmptyState(data: List<Barang>) {
        if (!isViewSafe()) return

        if (data.isEmpty()) {
            b.layoutProdukKosong.visibility = View.VISIBLE
            b.rvProdukKategori.visibility = View.GONE
        } else {
            b.layoutProdukKosong.visibility = View.GONE
            b.rvProdukKategori.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _b?.progressProdukKategori?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showDialogDetailProduk(barang: Barang) {
        if (!isViewSafe()) return

        val ctx = context ?: return
        val dialogBinding = DialogDetailProdukBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(ctx)
            .setView(dialogBinding.root)
            .create()

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val nama = barang.nama_barang ?: "-"
        val harga = parseHarga(barang.harga)
        val stok = barang.stok ?: 0
        val gambarUrl = barang.gambar_url
            ?.replace("\\/", "/")
            ?.replace(" ", "%20")

        var qty = if (stok > 0) 1 else 0

        dialogBinding.tvDetailNamaProduk.text = nama.uppercase()
        dialogBinding.tvDetailHargaProduk.text = formatRupiah(harga)
        dialogBinding.tvDetailKategori.text = "Kategori: ${barang.kategori?.nama_kategori ?: "-"}"
        dialogBinding.tvDetailBahan.text = "Bahan: ${barang.bahan?.nama_bahan ?: "-"}"
        dialogBinding.tvDetailUkuran.text = "Ukuran: ${barang.ukuran ?: "-"}"
        dialogBinding.tvDetailDeskripsi.text = barang.deskripsi ?: "-"
        dialogBinding.etQtyProduk.setText(qty.toString())

        Glide.with(dialogBinding.imgDetailProduk)
            .load(gambarUrl)
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(dialogBinding.imgDetailProduk)

        fun updateTotal() {
            dialogBinding.etQtyProduk.setText(qty.toString())
            dialogBinding.tvTotalProduk.text = "Total: ${formatRupiah(harga * qty)}"
        }

        if (stok > 0) {
            dialogBinding.tvDetailStok.text = "✔ Tersedia ($stok)"
            dialogBinding.tvDetailStok.setTextColor(Color.parseColor("#1F7A1F"))
            dialogBinding.btnAddCartDialog.isEnabled = true
            dialogBinding.btnAddCartDialog.alpha = 1f
            dialogBinding.btnAddCartDialog.text = "ADD TO CART"
        } else {
            dialogBinding.tvDetailStok.text = "✘ Stok Habis"
            dialogBinding.tvDetailStok.setTextColor(Color.parseColor("#C0392B"))
            dialogBinding.btnAddCartDialog.isEnabled = false
            dialogBinding.btnAddCartDialog.alpha = 0.5f
            dialogBinding.btnAddCartDialog.text = "STOK HABIS"
        }

        updateTotal()

        dialogBinding.btnCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnMinusQty.setOnClickListener {
            if (stok <= 0) return@setOnClickListener

            if (qty > 1) {
                qty--
                updateTotal()
            }
        }

        dialogBinding.btnPlusQty.setOnClickListener {
            if (stok <= 0) return@setOnClickListener

            if (qty < stok) {
                qty++
                updateTotal()
            } else {
                Toast.makeText(ctx, "Maksimal stok $stok", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.etQtyProduk.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputQty = dialogBinding.etQtyProduk.text.toString().toIntOrNull() ?: 1

                qty = when {
                    stok <= 0 -> 0
                    inputQty < 1 -> 1
                    inputQty > stok -> stok
                    else -> inputQty
                }

                updateTotal()
            }
        }

        dialogBinding.btnAddCartDialog.setOnClickListener {
            val barangId = barang.id

            if (!session.isLogin()) {
                Toast.makeText(ctx, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (barangId == null) {
                Toast.makeText(ctx, "ID barang tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (qty <= 0) {
                Toast.makeText(ctx, "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tambahKeKeranjang(barangId, qty, dialog)
        }

        dialog.show()
    }

    private fun tambahKeKeranjang(barangId: Int, jumlah: Int, dialog: AlertDialog) {
        val userId = session.getUserId()

        if (userId == 0) {
            showToast("User ID tidak ditemukan", Toast.LENGTH_SHORT)
            return
        }

        ApiClient.instance.tambahKeranjangQty(userId, barangId, jumlah)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    if (call.isCanceled || !isViewSafe()) return

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        showToast(body.message, Toast.LENGTH_SHORT)
                        dialog.dismiss()
                    } else {
                        showToast(
                            body?.message ?: "Gagal menambahkan ke keranjang",
                            Toast.LENGTH_SHORT
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    if (call.isCanceled || !isViewSafe()) return

                    showToast(
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    )
                }
            })
    }

    private fun parseHarga(value: String?): Int {
        if (value.isNullOrEmpty()) return 0

        return value
            .replace("Rp", "")
            .replace(".", "")
            .replace(",", "")
            .trim()
            .toIntOrNull() ?: 0
    }

    private fun formatRupiah(value: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(value).replace(",00", "")
    }

    private fun String.toSlug(): String {
        return this
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), "-")
    }
    private fun isViewSafe(): Boolean {
        return _b != null && isAdded
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        val ctx = context ?: return
        Toast.makeText(ctx, message, duration).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}