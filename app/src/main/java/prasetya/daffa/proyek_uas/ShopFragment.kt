package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import prasetya.daffa.proyek_uas.adapter.KategoriAdapter
import prasetya.daffa.proyek_uas.adapter.ProdukAdapter
import prasetya.daffa.proyek_uas.adapter.SliderAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.api.BarangListResponse
import prasetya.daffa.proyek_uas.api.Kategori
import prasetya.daffa.proyek_uas.api.KategoriResponse
import prasetya.daffa.proyek_uas.databinding.ShopFragmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShopFragment : Fragment() {

    private var _b: ShopFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var kategoriAdapter: KategoriAdapter
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
            kategoriTerpilih = kategori
            applyFilterAndSort()
        }

        produkAdapter = ProdukAdapter(mutableListOf()) { barang ->
            Toast.makeText(
                requireContext(),
                "${barang.nama_barang ?: "Produk"} ditambahkan ke keranjang",
                Toast.LENGTH_SHORT
            ).show()
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
        showLoading(true)

        ApiClient.instance.getKategori().enqueue(object : Callback<KategoriResponse> {
            override fun onResponse(
                call: Call<KategoriResponse>,
                response: Response<KategoriResponse>
            ) {
                showLoading(false)

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    kategoriAdapter.setData(body.data)
                } else {
                    Toast.makeText(
                        requireContext(),
                        body?.message ?: "Gagal mengambil data kategori",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<KategoriResponse>, t: Throwable) {
                showLoading(false)

                Toast.makeText(
                    requireContext(),
                    "Koneksi kategori gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun loadBarang() {
        showLoading(true)

        ApiClient.instance.getBarang().enqueue(object : Callback<BarangListResponse> {
            override fun onResponse(
                call: Call<BarangListResponse>,
                response: Response<BarangListResponse>
            ) {
                showLoading(false)

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    semuaBarang.clear()
                    semuaBarang.addAll(body.data)
                    applyFilterAndSort()
                } else {
                    Toast.makeText(
                        requireContext(),
                        body?.message ?: "Gagal mengambil data barang",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<BarangListResponse>, t: Throwable) {
                showLoading(false)

                Toast.makeText(
                    requireContext(),
                    "Koneksi barang gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun applyFilterAndSort() {
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
        b.progressProduk.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}