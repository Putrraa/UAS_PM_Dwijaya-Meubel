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
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.adapter.DataBarangAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Bahan
import prasetya.daffa.proyek_uas.api.BahanResponse
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.api.BarangListResponse
import prasetya.daffa.proyek_uas.api.BarangResponse
import prasetya.daffa.proyek_uas.api.Kategori
import prasetya.daffa.proyek_uas.api.KategoriResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.DialogEditBarangBinding
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
    private var selectedEditImageUri: Uri? = null
    private var selectedKategoriImageUri: Uri? = null

    private var dialogBarangBinding: DialogTambahBarangBinding? = null
    private var dialogEditBarangBinding: DialogEditBarangBinding? = null
    private var dialogKategoriBinding: DialogTambahKategoriBinding? = null

    private val pilihGambarLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                dialogBarangBinding?.imgPreviewBarang?.setImageURI(uri)
            }
        }

    private val pilihGambarEditLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedEditImageUri = uri
                dialogEditBarangBinding?.imgPreviewEditBarang?.setImageURI(uri)
            }
        }

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
                showDialogEditBarang(barang)
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

                Toast.makeText(
                    requireContext(),
                    "Gagal load kategori: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
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

                Toast.makeText(
                    requireContext(),
                    "Gagal load bahan: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
            semuaKategori.map { it.nama_kategori ?: "-" }
        )

        val bahanAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            semuaBahan.map { it.nama_bahan ?: "-" }
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

    private fun showDialogEditBarang(barang: Barang) {
        if (semuaKategori.isEmpty()) {
            Toast.makeText(requireContext(), "Kategori belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        if (semuaBahan.isEmpty()) {
            Toast.makeText(requireContext(), "Bahan belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val barangId = barang.id

        if (barangId == null) {
            Toast.makeText(requireContext(), "ID barang tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        selectedEditImageUri = null

        val dialogBinding = DialogEditBarangBinding.inflate(layoutInflater)
        dialogEditBarangBinding = dialogBinding

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val kategoriAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            semuaKategori.map { it.nama_kategori ?: "-" }
        )

        val bahanAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            semuaBahan.map { it.nama_bahan ?: "-" }
        )

        dialogBinding.spinnerEditKategoriBarang.adapter = kategoriAdapter
        dialogBinding.spinnerEditBahanBarang.adapter = bahanAdapter

        dialogBinding.edtEditNamaBarang.setText(barang.nama_barang.orEmpty())
        dialogBinding.edtEditHargaBarang.setText(barang.harga.toHargaEdit())
        dialogBinding.edtEditStokBarang.setText((barang.stok ?: 0).toString())
        dialogBinding.edtEditUkuranBarang.setText(barang.ukuran.orEmpty())
        dialogBinding.edtEditDeskripsiBarang.setText(barang.deskripsi.orEmpty())

        val gambarUrl = barang.gambar_url
            ?.replace("\\/", "/")
            ?.replace(" ", "%20")

        Glide.with(dialogBinding.imgPreviewEditBarang)
            .load(gambarUrl)
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(dialogBinding.imgPreviewEditBarang)

        val kategoriIdLama = barang.kategori_id ?: barang.kategori?.id
        val kategoriIndex = semuaKategori.indexOfFirst { it.id == kategoriIdLama }

        if (kategoriIndex >= 0) {
            dialogBinding.spinnerEditKategoriBarang.setSelection(kategoriIndex)
        }

        val bahanIdLama = barang.bahan_id ?: barang.bahan?.id
        val bahanIndex = semuaBahan.indexOfFirst { it.id == bahanIdLama }

        if (bahanIndex >= 0) {
            dialogBinding.spinnerEditBahanBarang.setSelection(bahanIndex)
        }

        dialogBinding.btnPilihGambarEditBarang.setOnClickListener {
            pilihGambarEditLauncher.launch("image/*")
        }

        dialogBinding.btnBatalEditBarang.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnUpdateBarang.setOnClickListener {
            val nama = dialogBinding.edtEditNamaBarang.text.toString().trim()
            val harga = dialogBinding.edtEditHargaBarang.text.toString().trim()
            val stok = dialogBinding.edtEditStokBarang.text.toString().trim()
            val ukuran = dialogBinding.edtEditUkuranBarang.text.toString().trim()
            val deskripsi = dialogBinding.edtEditDeskripsiBarang.text.toString().trim()

            val kategoriPosition = dialogBinding.spinnerEditKategoriBarang.selectedItemPosition
            val bahanPosition = dialogBinding.spinnerEditBahanBarang.selectedItemPosition

            if (nama.isEmpty()) {
                dialogBinding.edtEditNamaBarang.error = "Nama barang wajib diisi"
                return@setOnClickListener
            }

            if (harga.isEmpty()) {
                dialogBinding.edtEditHargaBarang.error = "Harga wajib diisi"
                return@setOnClickListener
            }

            if (stok.isEmpty()) {
                dialogBinding.edtEditStokBarang.error = "Stok wajib diisi"
                return@setOnClickListener
            }

            val kategoriId = semuaKategori[kategoriPosition].id
            val bahanId = semuaBahan[bahanPosition].id

            updateBarang(
                id = barangId,
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
            dialogEditBarangBinding = null
            selectedEditImageUri = null
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
                    val errorText = response.errorBody()?.string()

                    Toast.makeText(
                        requireContext(),
                        body?.message ?: errorText ?: "Gagal menambahkan barang",
                        Toast.LENGTH_LONG
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

    private fun updateBarang(
        id: Int,
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

        val gambarPart = selectedEditImageUri?.let {
            uriToMultipart(it, "gambar")
        }

        ApiClient.instance.updateBarang(
            id = id,
            namaBarang = nama.toTextRequestBody(),
            kategoriId = kategoriId.toString().toTextRequestBody(),
            bahanId = bahanId.toString().toTextRequestBody(),
            harga = harga.toTextRequestBody(),
            stok = stok.toTextRequestBody(),
            ukuran = ukuran.toTextRequestBody(),
            deskripsi = deskripsi.toTextRequestBody(),
            gambar = gambarPart
        ).enqueue(object : Callback<BarangResponse> {
            override fun onResponse(
                call: Call<BarangResponse>,
                response: Response<BarangResponse>
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
                    val errorText = response.errorBody()?.string()

                    Toast.makeText(
                        requireContext(),
                        body?.message ?: errorText ?: "Gagal mengupdate barang",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<BarangResponse>, t: Throwable) {
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
                    val errorText = response.errorBody()?.string()

                    Toast.makeText(
                        requireContext(),
                        body?.message ?: errorText ?: "Gagal menambah kategori",
                        Toast.LENGTH_LONG
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
                        val errorText = response.errorBody()?.string()

                        Toast.makeText(
                            requireContext(),
                            body?.message ?: errorText ?: "Gagal menambah bahan",
                            Toast.LENGTH_LONG
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
                    val errorText = response.errorBody()?.string()

                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: errorText ?: "Gagal menghapus barang",
                        Toast.LENGTH_LONG
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

    private fun String?.toHargaEdit(): String {
        return this
            ?.replace("Rp", "")
            ?.replace(".", "")
            ?.replace(",", "")
            ?.trim()
            ?: ""
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
        dialogEditBarangBinding = null
        dialogKategoriBinding = null

        selectedImageUri = null
        selectedEditImageUri = null
        selectedKategoriImageUri = null

        _b = null
    }
}