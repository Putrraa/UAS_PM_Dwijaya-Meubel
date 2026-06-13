package prasetya.daffa.proyek_uas

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.CustomFragmentBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class CustomFragment : Fragment() {

    private var _b: CustomFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var session: SessionManager
    private var selectedImageUri: Uri? = null

    private val pilihGambarLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri

                b.imgReferensiPreview.setImageURI(uri)
                b.imgReferensiPreview.visibility = View.VISIBLE
                b.btnHapusGambar.visibility = View.VISIBLE
                b.layoutUploadPlaceholder.visibility = View.GONE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = CustomFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())

        b.layoutUploadGambar.setOnClickListener {
            pilihGambarLauncher.launch("image/*")
        }

        b.btnHapusGambar.setOnClickListener {
            hapusGambar()
        }

        b.btnKirimPesanan.setOnClickListener {
            kirimCustomOrder()
        }
    }

    private fun kirimCustomOrder() {
        val jenisFurniture = b.etJenisFurniture.text.toString().trim()
        val jenisKayu = b.etJenisKayu.text.toString().trim()
        val ukuran = b.etUkuran.text.toString().trim()
        val catatan = b.etCatatanTambahan.text.toString().trim()

        if (jenisFurniture.isEmpty()) {
            b.etJenisFurniture.error = "Jenis furniture wajib diisi"
            b.etJenisFurniture.requestFocus()
            return
        }

        if (jenisKayu.isEmpty()) {
            b.etJenisKayu.error = "Jenis kayu wajib diisi"
            b.etJenisKayu.requestFocus()
            return
        }

        if (ukuran.isEmpty()) {
            b.etUkuran.error = "Ukuran wajib diisi"
            b.etUkuran.requestFocus()
            return
        }

        val userId = session.getUserId()

        if (userId == 0) {
            Toast.makeText(
                requireContext(),
                "User belum login. Silakan login ulang.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        b.btnKirimPesanan.isEnabled = false
        b.btnKirimPesanan.text = "Mengirim..."

        val userIdBody = userId.toString().toTextRequestBody()
        val jenisFurnitureBody = jenisFurniture.toTextRequestBody()
        val jenisKayuBody = jenisKayu.toTextRequestBody()
        val ukuranBody = ukuran.toTextRequestBody()
        val catatanBody = catatan.toTextRequestBody()

        val gambarPart = selectedImageUri?.let { uri ->
            uriToMultipart("gambar", uri)
        }

        ApiClient.instance.tambahCustomOrder(
            userId = userIdBody,
            jenisFurniture = jenisFurnitureBody,
            jenisKayu = jenisKayuBody,
            ukuran = ukuranBody,
            catatan = catatanBody,
            gambar = gambarPart
        ).enqueue(object : Callback<ResponseDefault> {

            override fun onResponse(
                call: Call<ResponseDefault>,
                response: Response<ResponseDefault>
            ) {
                b.btnKirimPesanan.isEnabled = true
                b.btnKirimPesanan.text = "+ KIRIM PESANAN"

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    Toast.makeText(
                        requireContext(),
                        body.message,
                        Toast.LENGTH_LONG
                    ).show()

                    resetForm()
                } else {
                    Toast.makeText(
                        requireContext(),
                        body?.message ?: "Gagal mengirim pesanan custom",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                b.btnKirimPesanan.isEnabled = true
                b.btnKirimPesanan.text = "+ KIRIM PESANAN"

                Toast.makeText(
                    requireContext(),
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun resetForm() {
        b.etJenisFurniture.setText("")
        b.etJenisKayu.setText("")
        b.etUkuran.setText("")
        b.etCatatanTambahan.setText("")
        hapusGambar()
    }

    private fun hapusGambar() {
        selectedImageUri = null

        b.imgReferensiPreview.setImageDrawable(null)
        b.imgReferensiPreview.visibility = View.GONE
        b.btnHapusGambar.visibility = View.GONE
        b.layoutUploadPlaceholder.visibility = View.VISIBLE
    }

    private fun String.toTextRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun uriToMultipart(partName: String, uri: Uri): MultipartBody.Part {
        val file = uriToFile(uri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            partName,
            file.name,
            requestFile
        )
    }

    private fun uriToFile(uri: Uri): File {
        val fileName = getFileName(uri)
        val file = File(requireContext().cacheDir, fileName)

        requireContext().contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }

        return file
    }

    private fun getFileName(uri: Uri): String {
        var result = "custom_order_${System.currentTimeMillis()}.jpg"

        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}