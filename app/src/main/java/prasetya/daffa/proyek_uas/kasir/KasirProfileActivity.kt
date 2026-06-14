package prasetya.daffa.proyek_uas.kasir

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import prasetya.daffa.proyek_uas.LoginActivity
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Pengguna
import prasetya.daffa.proyek_uas.api.PenggunaResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.ActivityProfileKasirBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KasirProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileKasirBinding
    private lateinit var session: SessionManager

    private var currentUserId: Int = 0
    private var currentName: String = ""
    private var currentEmail: String = ""
    private var currentRole: String = "kasir"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityProfileKasirBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        if (!session.isLogin()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        window.statusBarColor = Color.parseColor("#EEF2EA")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        currentUserId = session.getUserId()

        setupView()
        setupButton()
        loadKasirFromDatabase()
    }

    private fun setupView() {
        b.etKasirUsername.isEnabled = false
        b.etKasirEmail.isEnabled = false

        tampilkanDataSession()
    }

    private fun setupButton() {
        b.btnBackKasirTop.setOnClickListener {
            finish()
        }

        b.btnBackKasir.setOnClickListener {
            finish()
        }

        b.btnPerbaruiPassword.setOnClickListener {
            validasiUpdatePassword()
        }
    }

    private fun loadKasirFromDatabase() {
        if (currentUserId == 0) {
            tampilkanDataSession()
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.getPengguna().enqueue(object : Callback<PenggunaResponse> {
            override fun onResponse(
                call: Call<PenggunaResponse>,
                response: Response<PenggunaResponse>
            ) {
                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    val userLogin = body.data.find { it.id == currentUserId }

                    if (userLogin != null) {
                        tampilkanDataUser(userLogin)
                    } else {
                        tampilkanDataSession()
                        Toast.makeText(
                            this@KasirProfileActivity,
                            "Data kasir tidak ditemukan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    tampilkanDataSession()
                    Toast.makeText(
                        this@KasirProfileActivity,
                        body?.message ?: "Gagal mengambil data kasir",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PenggunaResponse>, t: Throwable) {
                tampilkanDataSession()
                Toast.makeText(
                    this@KasirProfileActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun tampilkanDataUser(user: Pengguna) {
        currentName = user.name ?: "Kasir"
        currentEmail = user.email ?: "kasir@gmail.com"
        currentRole = user.role ?: "kasir"

        b.tvKasirName.text = currentName
        b.etKasirUsername.setText(currentName)
        b.etKasirEmail.setText(currentEmail)

        val initial = currentName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"
        b.tvKasirAvatarInitial.text = initial
    }

    private fun tampilkanDataSession() {
        currentName = session.getName().ifEmpty { "Kasir" }
        currentEmail = session.getEmail().ifEmpty { "kasir@gmail.com" }
        currentRole = "kasir"

        b.tvKasirName.text = currentName
        b.etKasirUsername.setText(currentName)
        b.etKasirEmail.setText(currentEmail)

        val initial = currentName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"
        b.tvKasirAvatarInitial.text = initial
    }

    private fun validasiUpdatePassword() {
        val passwordLama = b.etKasirPasswordLama.text.toString().trim()
        val passwordBaru = b.etKasirPasswordBaru.text.toString().trim()
        val konfirmasiPassword = b.etKasirKonfirmasiPassword.text.toString().trim()

        when {
            currentUserId == 0 -> {
                Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            }

            passwordLama.isEmpty() -> {
                b.etKasirPasswordLama.error = "Password lama wajib diisi"
                b.etKasirPasswordLama.requestFocus()
            }

            passwordBaru.isEmpty() -> {
                b.etKasirPasswordBaru.error = "Password baru wajib diisi"
                b.etKasirPasswordBaru.requestFocus()
            }

            passwordBaru.length < 6 -> {
                b.etKasirPasswordBaru.error = "Password minimal 6 karakter"
                b.etKasirPasswordBaru.requestFocus()
            }

            konfirmasiPassword.isEmpty() -> {
                b.etKasirKonfirmasiPassword.error = "Konfirmasi password wajib diisi"
                b.etKasirKonfirmasiPassword.requestFocus()
            }

            passwordBaru != konfirmasiPassword -> {
                b.etKasirKonfirmasiPassword.error = "Konfirmasi password tidak sama"
                b.etKasirKonfirmasiPassword.requestFocus()
            }

            else -> {
                updatePassword(passwordLama, passwordBaru, konfirmasiPassword)
            }
        }
    }

    private fun updatePassword(
        passwordLama: String,
        passwordBaru: String,
        konfirmasiPassword: String
    ) {
        b.btnPerbaruiPassword.isEnabled = false
        b.btnPerbaruiPassword.text = "Memproses..."

        ApiClient.instance.updatePasswordPengguna(
            currentUserId,
            passwordLama,
            passwordBaru,
            konfirmasiPassword
        ).enqueue(object : Callback<ResponseDefault> {
            override fun onResponse(
                call: Call<ResponseDefault>,
                response: Response<ResponseDefault>
            ) {
                b.btnPerbaruiPassword.isEnabled = true
                b.btnPerbaruiPassword.text = "Perbarui Password"

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    Toast.makeText(
                        this@KasirProfileActivity,
                        body.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    b.etKasirPasswordLama.text.clear()
                    b.etKasirPasswordBaru.text.clear()
                    b.etKasirKonfirmasiPassword.text.clear()
                } else {
                    Toast.makeText(
                        this@KasirProfileActivity,
                        body?.message ?: "Gagal memperbarui password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                b.btnPerbaruiPassword.isEnabled = true
                b.btnPerbaruiPassword.text = "Perbarui Password"

                Toast.makeText(
                    this@KasirProfileActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}