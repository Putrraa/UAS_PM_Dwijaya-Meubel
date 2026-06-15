package prasetya.daffa.proyek_uas.fragment

import android.app.AlertDialog
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import prasetya.daffa.proyek_uas.adapter.DataBarangAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Bahan
import prasetya.daffa.proyek_uas.api.BahanResponse
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.api.BarangListResponse
import prasetya.daffa.proyek_uas.api.Kategori
import prasetya.daffa.proyek_uas.api.KategoriResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.DialogTambahBahanBinding
import prasetya.daffa.proyek_uas.databinding.DialogTambahBarangBinding
import prasetya.daffa.proyek_uas.databinding.DialogTambahKategoriBinding
import prasetya.daffa.proyek_uas.databinding.KelolaBarangFragmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KelolaBarangFragment : Fragment() {

    private var _b: KelolaBarangFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var adapter: DataBarangAdapter

    private val semuaBarang = mutableListOf<Barang>()
    private val semuaKategori = mutableListOf<Kategori>()
    private val semuaBahan = mutableListOf<Bahan>()

    private var selectedImageUri: Uri? = null
    private var dialogBarangBinding: DialogTambahBarangBinding? = null

    private val pilihGambarLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                dialogBarangBinding?.imgPreviewBarang?.setImageURI(uri)
            }
        }
    private var selectedKategoriImageUri: Uri? = null
    private var dialogKategoriBinding: DialogTambahKategoriBinding? = null

    private val pilihGambarKategoriLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedKategoriImageUri = uri
                dialogKategoriBinding?.imgPreviewKategori?.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = KelolaBarangFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupButton()

        loadKategori()
        loadBahan()
        loadBarang()
    }

    private fun setupRecyclerView() {
        adapter = DataBarangAdapter(
            mutableListOf(),
            onEdit = { barang ->
                Toast.makeText(
                    requireContext(),
                    "Edit ${barang.nama_barang}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onHapus = { barang ->
                konfirmasiHapus(barang)
            }
        )

        b.rvBarang.layoutManager = LinearLayoutManager(requireContext())
        b.rvBarang.adapter = adapter
    }

    private fun setupButton() {
        b.btnTambahBarang.setOnClickListener {
            showDialogTambahBarang()
        }

        b.btnTambahKategori.setOnClickListener {
            showDialogTambahKategori()
        }

        b.btnTambahBahan.setOnClickListener {
            showDialogTambahBahan()
        }

        b.btnCariBarang.setOnClickListener {
            cariBarang()
        }

        b.btnResetCari.setOnClickListener {
            b.edtCariBarang.setText("")
            adapter.setData(semuaBarang)
            cekDataKosong(semuaBarang)
        }
    }

    private fun loadBarang() {
        if (_b == null) return

        b.progressBar.visibility = View.VISIBLE
        b.tvKosong.visibility = View.GONE

        ApiClient.instance.getBarang().enqueue(object : Callback<BarangListResponse> {
            override fun onResponse(
                call: Call<BarangListResponse>,
                response: Response<BarangListResponse>
            ) {
                if (_b == null || !isAdded) return

                b.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data ?: emptyList()

                    semuaBarang.clear()
                    semuaBarang.addAll(data)

                    adapter.setData(semuaBarang)
                    cekDataKosong(semuaBarang)
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Gagal mengambil data barang",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<BarangListResponse>, t: Throwable) {
                if (_b == null || !isAdded) return

                b.progressBar.visibility = View.GONE

                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun loadKategori() {
        ApiClient.instance.getKategori().enqueue(object : Callback<KategoriResponse> {
            override fun onResponse(
                call: Call<KategoriResponse>,
                response: Response<KategoriResponse>
            ) {
                if (_b == null || !isAdded) return

                if (response.isSuccessful && response.body()?.status == true) {
                    semuaKategori.clear()
                    semuaKategori.addAll(response.body()?.data ?: emptyList())
                }
            }

            override fun onFailure(call: Call<KategoriResponse>, t: Throwable) {
                if (_b == null || !isAdded) return
                Toast.makeText(requireContext(), "Gagal load kategori: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadBahan() {
        ApiClient.instance.getBahan().enqueue(object : Callback<BahanResponse> {
            override fun onResponse(
                call: Call<BahanResponse>,
                response: Response<BahanResponse>
            ) {
                if (_b == null || !isAdded) return

                if (response.isSuccessful && response.body()?.status == true) {
                    semuaBahan.clear()
                    semuaBahan.addAll(response.body()?.data ?: emptyList())
                }
            }

            override fun onFailure(call: Call<BahanResponse>, t: Throwable) {
                if (_b == null || !isAdded) return
                Toast.makeText(requireContext(), "Gagal load bahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDialogTambahBarang() {
        if (semuaKategori.isEmpty()) {
            Toast.makeText(requireContext(), "Kategori masih kosong, tambahkan kategori dulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (semuaBahan.isEmpty()) {
            Toast.makeText(requireContext(), "Bahan masih kosong, tambahkan bahan dulu", Toast.LENGTH_SHORT).show()
            return
        }

        selectedImageUri = null

        val dialogBinding = DialogTambahBarangBinding.inflate(layoutInflater)
        dialogBarangBinding = dialogBinding

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val kategoriAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            semuaKategori.map { it.nama_kategori }
        )

        val bahanAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            semuaBahan.map { it.nama_bahan }
        )

        dialogBinding.spinnerKategoriBarang.adapter = kategoriAdapter
        dialogBinding.spinnerBahanBarang.adapter = bahanAdapter

        dialogBinding.btnPilihGambarBarang.setOnClickListener {
            pilihGambarLauncher.launch("image/*")
        }

        dialogBinding.btnBatalBarang.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSimpanBarang.setOnClickListener {
            val nama = dialogBinding.edtNamaBarang.text.toString().trim()
            val harga = dialogBinding.edtHargaBarang.text.toString().trim()
            val stok = dialogBinding.edtStokBarang.text.toString().trim()
            val ukuran = dialogBinding.edtUkuranBarang.text.toString().trim()
            val deskripsi = dialogBinding.edtDeskripsiBarang.text.toString().trim()

            val kategoriPosition = dialogBinding.spinnerKategoriBarang.selectedItemPosition
            val bahanPosition = dialogBinding.spinnerBahanBarang.selectedItemPosition

            if (nama.isEmpty()) {
                dialogBinding.edtNamaBarang.error = "Nama barang wajib diisi"
                return@setOnClickListener
            }

            if (harga.isEmpty()) {
                dialogBinding.edtHargaBarang.error = "Harga wajib diisi"
                return@setOnClickListener
            }

            if (stok.isEmpty()) {
                dialogBinding.edtStokBarang.error = "Stok wajib diisi"
                return@setOnClickListener
            }

            val kategoriId = semuaKategori[kategoriPosition].id
            val bahanId = semuaBahan[bahanPosition].id

            simpanBarang(
                nama = nama,
                kategoriId = kategoriId,
                bahanId = bahanId,
                harga = harga,
                stok = stok,
                ukuran = ukuran,
                deskripsi = deskripsi,
                dialog = dialog
            )
        }

        dialog.setOnDismissListener {
            dialogBarangBinding = null
            selectedImageUri = null
        }

        dialog.show()
    }

    private fun simpanBarang(
        nama: String,
        kategoriId: Int,
        bahanId: Int,
        harga: String,
        stok: String,
        ukuran: String,
        deskripsi: String,
        dialog: AlertDialog
    ) {
        b.progressBar.visibility = View.VISIBLE

        val gambarPart = selectedImageUri?.let {
            uriToMultipart(it, "gambar")
        }

        ApiClient.instance.tambahBarang(
            namaBarang = nama.toTextRequestBody(),
            kategoriId = kategoriId.toString().toTextRequestBody(),
            bahanId = bahanId.toString().toTextRequestBody(),
            harga = harga.toTextRequestBody(),
            stok = stok.toTextRequestBody(),
            ukuran = ukuran.toTextRequestBody(),
            deskripsi = deskripsi.toTextRequestBody(),
            gambar = gambarPart
        ).enqueue(object : Callback<ResponseDefault> {
            override fun onResponse(
                call: Call<ResponseDefault>,
                response: Response<ResponseDefault>
            ) {
                if (_b == null || !isAdded) return

                b.progressBar.visibility = View.GONE

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    Toast.makeText(
                        requireContext(),
                        body.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                    loadBarang()
                } else {
                    Toast.makeText(
                        requireContext(),
                        body?.message ?: "Gagal menambahkan barang",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                if (_b == null || !isAdded) return

                b.progressBar.visibility = View.GONE

                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun showDialogTambahKategori() {
        selectedKategoriImageUri = null

        val dialogBinding = DialogTambahKategoriBinding.inflate(layoutInflater)
        dialogKategoriBinding = dialogBinding

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnPilihGambarKategori.setOnClickListener {
            pilihGambarKategoriLauncher.launch("image/*")
        }

        dialogBinding.btnBatalKategori.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSimpanKategori.setOnClickListener {
            val namaKategori = dialogBinding.edtNamaKategori.text.toString().trim()

            if (namaKategori.isEmpty()) {
                dialogBinding.edtNamaKategori.error = "Nama kategori wajib diisi"
                return@setOnClickListener
            }

            simpanKategori(namaKategori, dialog)
        }

        dialog.setOnDismissListener {
            dialogKategoriBinding = null
            selectedKategoriImageUri = null
        }

        dialog.show()
    }

    private fun simpanKategori(namaKategori: String, dialog: AlertDialog) {
        b.progressBar.visibility = View.VISIBLE

        val gambarPart = selectedKategoriImageUri?.let {
            uriToMultipart(it, "gambar")
        }

        ApiClient.instance.tambahKategori(
            namaKategori = namaKategori.toTextRequestBody(),
            gambar = gambarPart
        ).enqueue(object : Callback<ResponseDefault> {
            override fun onResponse(
                call: Call<ResponseDefault>,
                response: Response<ResponseDefault>
            ) {
                if (_b == null || !isAdded) return

                b.progressBar.visibility = View.GONE

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadKategori()
                } else {
                    Toast.makeText(
                        requireContext(),
                        body?.message ?: "Gagal menambah kategori",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                if (_b == null || !isAdded) return

                b.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun showDialogTambahBahan() {
        val dialogBinding = DialogTambahBahanBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnBatalBahan.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSimpanBahan.setOnClickListener {
            val namaBahan = dialogBinding.edtNamaBahan.text.toString().trim()

            if (namaBahan.isEmpty()) {
                dialogBinding.edtNamaBahan.error = "Nama bahan wajib diisi"
                return@setOnClickListener
            }

            simpanBahan(namaBahan, dialog)
        }

        dialog.show()
    }

    private fun simpanBahan(namaBahan: String, dialog: AlertDialog) {
        b.progressBar.visibility = View.VISIBLE

        ApiClient.instance.tambahBahan(namaBahan)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    if (_b == null || !isAdded) return

                    b.progressBar.visibility = View.GONE

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadBahan()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal menambah bahan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    if (_b == null || !isAdded) return

                    b.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun cariBarang() {
        val keyword = b.edtCariBarang.text.toString().trim().lowercase()

        if (keyword.isEmpty()) {
            adapter.setData(semuaBarang)
            cekDataKosong(semuaBarang)
            return
        }

        val hasil = semuaBarang.filter {
            it.nama_barang?.lowercase()?.contains(keyword) == true ||
                    it.kategori?.nama_kategori?.lowercase()?.contains(keyword) == true ||
                    it.bahan?.nama_bahan?.lowercase()?.contains(keyword) == true
        }

        adapter.setData(hasil)
        cekDataKosong(hasil)
    }

    private fun konfirmasiHapus(barang: Barang) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Barang")
            .setMessage("Yakin ingin menghapus ${barang.nama_barang}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusBarang(barang)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusBarang(barang: Barang) {
        val id = barang.id ?: return

        b.progressBar.visibility = View.VISIBLE

        ApiClient.instance.deleteBarang(id).enqueue(object : Callback<ResponseDefault> {
            override fun onResponse(
                call: Call<ResponseDefault>,
                response: Response<ResponseDefault>
            ) {
                if (_b == null || !isAdded) return

                b.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(
                        requireContext(),
                        "Barang berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadBarang()
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Gagal menghapus barang",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                if (_b == null || !isAdded) return

                b.progressBar.visibility = View.GONE

                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun cekDataKosong(data: List<Barang>) {
        b.tvKosong.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
        b.rvBarang.visibility = if (data.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun String.toTextRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun uriToMultipart(uri: Uri, partName: String): MultipartBody.Part {
        val fileName = getFileName(uri) ?: "gambar_${System.currentTimeMillis()}.jpg"
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: byteArrayOf()
        inputStream?.close()

        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            partName,
            fileName,
            requestBody
        )
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null

        if (uri.scheme == "content") {
            val cursor: Cursor? = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = it.getString(index)
                    }
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogBarangBinding = null
        _b = null
    }
}