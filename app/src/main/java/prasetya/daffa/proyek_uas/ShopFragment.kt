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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import prasetya.daffa.proyek_uas.adapter.KategoriAdapter
import prasetya.daffa.proyek_uas.adapter.ProdukAdapter
import prasetya.daffa.proyek_uas.adapter.SliderAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.api.BarangListResponse
import prasetya.daffa.proyek_uas.api.Kategori
import prasetya.daffa.proyek_uas.api.KategoriResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.DialogDetailProdukBinding
import prasetya.daffa.proyek_uas.databinding.ShopFragmentBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class ShopFragment : Fragment() {

    private var _b: ShopFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var kategoriAdapter: KategoriAdapter
    private lateinit var session: SessionManager
    private lateinit var produkAdapter: ProdukAdapter

    private val semuaBarang = mutableListOf<Barang>()

    private var kategoriTerpilih: Kategori? = null
    private var keywordSearch = ""
    private var sortTerpilih = "Default"

    private val pilihanSort = listOf(
        "Default",
        "Harga Terendah",
        "Harga Tertinggi",
        "Nama A-Z",
        "Nama Z-A",
        "Stok Terbanyak"
    )



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = ShopFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        setupSlider()
        setupRecyclerView()
        setupSort()
        setupSearch()

        loadKategori()
        loadBarang()
    }

    private fun setupSlider() {
        val sliderImages = listOf(
            R.drawable.slide1,
            R.drawable.slide2,
            R.drawable.slide3,
            R.drawable.slide4,
            R.drawable.slide5
        )

        b.vpSlider.adapter = SliderAdapter(sliderImages)
    }

    private fun setupRecyclerView() {
        kategoriAdapter = KategoriAdapter(mutableListOf()) { kategori ->
            val slug = kategori.nama_kategori.orEmpty().toSlug()

            if (slug.isEmpty()) {
                showToast("Nama kategori tidak valid", Toast.LENGTH_SHORT)
                return@KategoriAdapter
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, KategoriProdukFragment.newInstance(slug))
                .addToBackStack(null)
                .commit()
        }

        produkAdapter = ProdukAdapter(mutableListOf()) { barang ->
            showDialogDetailProduk(barang)
        }

        b.rvKategori.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rvKategori.adapter = kategoriAdapter

        b.rvProduk.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rvProduk.adapter = produkAdapter
    }

    private fun setupSort() {
        val adapterSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            pilihanSort
        )

        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spinnerSortProduk.adapter = adapterSpinner

        b.spinnerSortProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sortTerpilih = pilihanSort[position]
                applyFilterAndSort()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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
        val gambarUrl = barang.gambar_url?.replace("\\/", "/")?.replace(" ", "%20")

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
                showToast("Silakan login terlebih dahulu", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }

            if (barangId == null) {
                showToast("ID barang tidak valid", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }

            if (qty <= 0) {
                showToast("Jumlah tidak valid", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }

            tambahKeKeranjang(barangId, qty, dialog)
        }

        dialog.show()
    }

    private fun setupSearch() {
        b.btnSearchProduk.setOnClickListener {
            keywordSearch = b.etSearchProduk.text.toString().trim()
            applyFilterAndSort()
        }

        b.etSearchProduk.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                keywordSearch = s.toString().trim()
                applyFilterAndSort()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadKategori() {
        if (!isViewSafe()) return

        showLoading(true)

        ApiClient.instance.getKategori().enqueue(object : Callback<KategoriResponse> {
            override fun onResponse(
                call: Call<KategoriResponse>,
                response: Response<KategoriResponse>
            ) {
                if (call.isCanceled || !isViewSafe()) return

                showLoading(false)

                val body = response.body()
                val data = body?.data.orEmpty()

                if (response.isSuccessful && body?.status == true) {
                    kategoriAdapter.setData(data)
                } else {
                    showToast(
                        body?.message ?: "Gagal mengambil data kategori",
                        Toast.LENGTH_SHORT
                    )
                }
            }

            override fun onFailure(call: Call<KategoriResponse>, t: Throwable) {
                if (call.isCanceled || !isViewSafe()) return

                showLoading(false)
                showToast("Koneksi kategori gagal: ${t.message}", Toast.LENGTH_LONG)
            }
        })
    }

    private fun loadBarang() {
        if (!isViewSafe()) return

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
                    semuaBarang.clear()
                    semuaBarang.addAll(data)
                    applyFilterAndSort()
                } else {
                    showToast(
                        body?.message ?: "Gagal mengambil data barang",
                        Toast.LENGTH_SHORT
                    )
                }
            }

            override fun onFailure(call: Call<BarangListResponse>, t: Throwable) {
                if (call.isCanceled || !isViewSafe()) return

                showLoading(false)
                showToast("Koneksi barang gagal: ${t.message}", Toast.LENGTH_LONG)
            }
        })
    }

    private fun applyFilterAndSort() {
        if (!isViewSafe() || !::produkAdapter.isInitialized) return

        var hasil = semuaBarang.toList()

        kategoriTerpilih?.let { kategori ->
            hasil = hasil.filter { barang ->
                barang.kategori?.id == kategori.id
            }
        }

        if (keywordSearch.isNotEmpty()) {
            hasil = hasil.filter { barang ->
                barang.nama_barang?.contains(keywordSearch, ignoreCase = true) == true ||
                        barang.kategori?.nama_kategori?.contains(keywordSearch, ignoreCase = true) == true ||
                        barang.bahan?.nama_bahan?.contains(keywordSearch, ignoreCase = true) == true
            }
        }

        hasil = when (sortTerpilih) {
            "Harga Terendah" -> hasil.sortedBy { it.harga.toHargaInt() }
            "Harga Tertinggi" -> hasil.sortedByDescending { it.harga.toHargaInt() }
            "Nama A-Z" -> hasil.sortedBy { it.nama_barang.orEmpty().lowercase() }
            "Nama Z-A" -> hasil.sortedByDescending { it.nama_barang.orEmpty().lowercase() }
            "Stok Terbanyak" -> hasil.sortedByDescending { it.stok ?: 0 }
            else -> hasil
        }

        produkAdapter.setData(hasil)
        updateInfo(hasil.size)
    }
    private fun String?.toHargaInt(): Int {
        return this
            ?.replace("Rp", "")
            ?.replace(".", "")
            ?.replace(",", "")
            ?.trim()
            ?.toIntOrNull() ?: 0
    }

    private fun updateInfo(total: Int) {
        if (!isViewSafe()) return

        b.tvResultInfo.text = "Showing $total products"

        if (total == 0) {
            b.tvProdukKosong.visibility = View.VISIBLE
            b.rvProduk.visibility = View.GONE
        } else {
            b.tvProdukKosong.visibility = View.GONE
            b.rvProduk.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _b?.progressProduk?.visibility = if (isLoading) View.VISIBLE else View.GONE
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

                    showToast("Koneksi gagal: ${t.message}", Toast.LENGTH_LONG)
                }
            })
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

    private fun String.toSlug(): String {
        return this
            .lowercase()
            .trim()
            .replace(Regex("\\s+"), "-")
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
}