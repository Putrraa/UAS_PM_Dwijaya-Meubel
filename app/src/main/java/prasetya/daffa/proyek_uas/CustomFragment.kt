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
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView


class CustomFragment : Fragment() {

    private var _b: CustomFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var session: SessionManager
    private var cameraImageBitmap: android.graphics.Bitmap? = null
    private var selectedImageUri: Uri? = null

    private val pilihGambarLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (!isViewSafe()) return@registerForActivityResult

            if (uri != null) {
                selectedImageUri = uri

                b.imgReferensiPreview.setImageURI(uri)
                b.imgReferensiPreview.visibility = View.VISIBLE
                b.btnHapusGambar.visibility = View.VISIBLE
                b.layoutUploadPlaceholder.visibility = View.GONE
            }
        }
    private val kameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (!isViewSafe()) return@registerForActivityResult

            if (bitmap != null) {
                cameraImageBitmap = bitmap
                selectedImageUri = null

                b.imgReferensiPreview.setImageBitmap(bitmap)
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
            showImagePickerDialog()
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
            showToast("User belum login. Silakan login ulang.", Toast.LENGTH_LONG)
            return
        }

        b.btnKirimPesanan.isEnabled = false
        b.btnKirimPesanan.text = getString(R.string.mengirim)

        val userIdBody = userId.toString().toTextRequestBody()
        val jenisFurnitureBody = jenisFurniture.toTextRequestBody()
        val jenisKayuBody = jenisKayu.toTextRequestBody()
        val ukuranBody = ukuran.toTextRequestBody()
        val catatanBody = catatan.toTextRequestBody()

        val gambarPart = when {
            selectedImageUri != null -> {
                uriToMultipart("gambar", selectedImageUri!!)
            }
            cameraImageBitmap != null -> {
                bitmapToMultipart("gambar", cameraImageBitmap!!)
            }
            else -> null
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
                if (call.isCanceled || !isViewSafe()) return

                setButtonLoading(false)

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    showToast(body.message, Toast.LENGTH_LONG)
                    resetForm()
                } else {
                    showToast(
                        body?.message ?: "Gagal mengirim pesanan custom",
                        Toast.LENGTH_LONG
                    )
                }
            }

            override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                if (call.isCanceled || !isViewSafe()) return

                setButtonLoading(false)
                showToast("Koneksi gagal: ${t.message}", Toast.LENGTH_LONG)
            }
        })
    }

    private fun resetForm() {
        if (!isViewSafe()) return

        b.etJenisFurniture.setText("")
        b.etJenisKayu.setText("")
        b.etUkuran.setText("")
        b.etCatatanTambahan.setText("")

        selectedImageUri = null
        cameraImageBitmap = null

        hapusGambar()
    }
    private fun hapusGambar() {
        selectedImageUri = null

        if (!isViewSafe()) return

        b.imgReferensiPreview.setImageDrawable(null)
        b.imgReferensiPreview.visibility = View.GONE
        b.btnHapusGambar.visibility = View.GONE
        b.layoutUploadPlaceholder.visibility = View.VISIBLE
    }

    private fun String.toTextRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun bitmapToMultipart(partName: String, bitmap: android.graphics.Bitmap): MultipartBody.Part {

        val file = File(requireContext().cacheDir, "camera_${System.currentTimeMillis()}.jpg")

        FileOutputStream(file).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            partName,
            file.name,
            requestFile
        )
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
    private fun isViewSafe(): Boolean {
        return _b != null && isAdded
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        val ctx = context ?: return
        Toast.makeText(ctx, message, duration).show()
    }

    private fun setButtonLoading(isLoading: Boolean) {
        if (!isViewSafe()) return

        b.btnKirimPesanan.isEnabled = !isLoading
        b.btnKirimPesanan.text = if (isLoading) "Mengirim..." else "+ KIRIM PESANAN"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
    private fun showImagePickerDialog() {
        val dialog = BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(R.layout.dialog_image_picker, null)

        val btnKamera = view.findViewById<View>(R.id.btnKamera)
        val btnGaleri = view.findViewById<View>(R.id.btnGaleri)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)

        btnKamera.setOnClickListener {
            kameraLauncher.launch(null)
            dialog.dismiss()
        }

        btnGaleri.setOnClickListener {
            pilihGambarLauncher.launch("image/*")
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}