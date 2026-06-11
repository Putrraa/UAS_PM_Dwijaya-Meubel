package prasetya.daffa.proyek_uas

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Bahan
import prasetya.daffa.proyek_uas.api.BahanResponse
import prasetya.daffa.proyek_uas.api.BarangResponse
import prasetya.daffa.proyek_uas.api.Kategori
import prasetya.daffa.proyek_uas.api.KategoriResponse
import prasetya.daffa.proyek_uas.databinding.ActivityTambahBarangAdminBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class TambahBarangAdminActivity : AppCompatActivity() {

    private lateinit var b: ActivityTambahBarangAdminBinding

    private var kategoriList: List<Kategori> = emptyList()
    private var bahanList: List<Bahan> = emptyList()

    private var selectedKategoriId: Int = 0
    private var selectedBahanId: Int = 0

    private var selectedImageUri: Uri? = null

    private val pilihGambarLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                b.imgPreview.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTambahBarangAdminBinding.inflate(layoutInflater)
        setContentView(b.root)

        loadKategori()
        loadBahan()

        b.btnPilihGambar.setOnClickListener {
            pilihGambarLauncher.launch("image/*")
        }

        b.btnSimpan.setOnClickListener {
            simpanBarang()
        }
    }

    private fun loadKategori() {
        b.progressBar.visibility = View.VISIBLE

        ApiClient.instance.getKategori().enqueue(object : Callback<KategoriResponse> {
            override fun onResponse(
                call: Call<KategoriResponse>,
                response: Response<KategoriResponse>
            ) {
                b.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == true) {
                    kategoriList = response.body()?.data ?: emptyList()

                    val namaKategori = kategoriList.map { it.nama_kategori }

                    val adapter = ArrayAdapter(
                        this@TambahBarangAdminActivity,
                        android.R.layout.simple_spinner_item,
                        namaKategori
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    b.spinnerKategori.adapter = adapter

                    b.spinnerKategori.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                selectedKategoriId = kategoriList[position].id
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                selectedKategoriId = 0
                            }
                        }
                } else {
                    Toast.makeText(
                        this@TambahBarangAdminActivity,
                        "Gagal mengambil kategori",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<KategoriResponse>, t: Throwable) {
                b.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@TambahBarangAdminActivity,
                    "Error kategori: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun loadBahan() {
        b.progressBar.visibility = View.VISIBLE

        ApiClient.instance.getBahan().enqueue(object : Callback<BahanResponse> {
            override fun onResponse(
                call: Call<BahanResponse>,
                response: Response<BahanResponse>
            ) {
                b.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == true) {
                    bahanList = response.body()?.data ?: emptyList()

                    val namaBahan = bahanList.map { it.nama_bahan }

                    val adapter = ArrayAdapter(
                        this@TambahBarangAdminActivity,
                        android.R.layout.simple_spinner_item,
                        namaBahan
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    b.spinnerBahan.adapter = adapter

                    b.spinnerBahan.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                selectedBahanId = bahanList[position].id
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                selectedBahanId = 0
                            }
                        }
                } else {
                    Toast.makeText(
                        this@TambahBarangAdminActivity,
                        "Gagal mengambil bahan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<BahanResponse>, t: Throwable) {
                b.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@TambahBarangAdminActivity,
                    "Error bahan: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun simpanBarang() {
        val namaBarang = b.edtNamaBarang.text.toString().trim()
        val harga = b.edtHarga.text.toString().trim()
        val stok = b.edtStok.text.toString().trim()
        val ukuran = b.edtUkuran.text.toString().trim()
        val deskripsi = b.edtDeskripsi.text.toString().trim()

        if (namaBarang.isEmpty()) {
            b.edtNamaBarang.error = "Nama barang wajib diisi"
            return
        }

        if (selectedKategoriId == 0) {
            Toast.makeText(this, "Kategori belum dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedBahanId == 0) {
            Toast.makeText(this, "Bahan belum dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        if (harga.isEmpty()) {
            b.edtHarga.error = "Harga wajib diisi"
            return
        }

        if (stok.isEmpty()) {
            b.edtStok.error = "Stok wajib diisi"
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Pilih gambar dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = uriToFile(selectedImageUri!!)

        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())

        val gambarPart = MultipartBody.Part.createFormData(
            "gambar",
            imageFile.name,
            requestFile
        )

        b.progressBar.visibility = View.VISIBLE
        b.btnSimpan.isEnabled = false

        ApiClient.instance.tambahBarang(
            namaBarang = text(namaBarang),
            kategoriId = text(selectedKategoriId.toString()),
            bahanId = text(selectedBahanId.toString()),
            harga = text(harga),
            stok = text(stok),
            ukuran = text(ukuran),
            deskripsi = text(deskripsi),
            gambar = gambarPart
        ).enqueue(object : Callback<BarangResponse> {
            override fun onResponse(
                call: Call<BarangResponse>,
                response: Response<BarangResponse>
            ) {
                b.progressBar.visibility = View.GONE
                b.btnSimpan.isEnabled = true

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(
                        this@TambahBarangAdminActivity,
                        response.body()?.message ?: "Barang berhasil ditambahkan",
                        Toast.LENGTH_LONG
                    ).show()

                    resetForm()
                } else {
                    val errorBody = response.errorBody()?.string()

                    Toast.makeText(
                        this@TambahBarangAdminActivity,
                        "Gagal simpan: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()

                    android.util.Log.e("SIMPAN_BARANG", errorBody ?: "Error kosong")
                }
            }

            override fun onFailure(call: Call<BarangResponse>, t: Throwable) {
                b.progressBar.visibility = View.GONE
                b.btnSimpan.isEnabled = true

                Toast.makeText(
                    this@TambahBarangAdminActivity,
                    "Error simpan: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()

                android.util.Log.e("SIMPAN_BARANG", "Failure", t)
            }
        })
    }

    private fun text(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun uriToFile(uri: Uri): File {
        val fileName = getFileName(uri)
        val tempFile = File(cacheDir, fileName)

        contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }

        return tempFile
    }

    private fun getFileName(uri: Uri): String {
        var name = "gambar_barang_${System.currentTimeMillis()}.jpg"

        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (it.moveToFirst() && nameIndex >= 0) {
                name = it.getString(nameIndex)
            }
        }

        return name
    }

    private fun resetForm() {
        b.edtNamaBarang.text.clear()
        b.edtHarga.text.clear()
        b.edtStok.text.clear()
        b.edtUkuran.text.clear()
        b.edtDeskripsi.text.clear()
        b.imgPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        selectedImageUri = null
    }
}