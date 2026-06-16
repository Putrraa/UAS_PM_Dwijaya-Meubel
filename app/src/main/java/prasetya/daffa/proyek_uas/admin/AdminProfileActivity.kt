package prasetya.daffa.proyek_uas.admin

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
import prasetya.daffa.proyek_uas.databinding.ActivityProfileAdminBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileAdminBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityProfileAdminBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

        if (!session.isLogin()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        window.statusBarColor = Color.parseColor("#F4F6F2")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        setupView()
        setupButton()
        loadAdminFromDatabase()
    }

    private fun setupView() {
        b.etAdminNama.isEnabled = false
        b.etAdminEmail.isEnabled = false

        tampilkanDataSession()
    }

    private fun setupButton() {
        b.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadAdminFromDatabase() {
        val userId = session.getUserId()

        if (userId == 0) {
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
                    val userLogin = body.data.find { it.id == userId }

                    if (userLogin != null) {
                        tampilkanDataUser(userLogin)
                    } else {
                        tampilkanDataSession()
                        Toast.makeText(
                            this@AdminProfileActivity,
                            "Data admin tidak ditemukan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    tampilkanDataSession()
                    Toast.makeText(
                        this@AdminProfileActivity,
                        body?.message ?: "Gagal mengambil data admin",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PenggunaResponse>, t: Throwable) {
                tampilkanDataSession()
                Toast.makeText(
                    this@AdminProfileActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun tampilkanDataUser(user: Pengguna) {
        val nama = user.name ?: "Admin"
        val email = user.email ?: "admin@gmail.com"
        val role = user.role ?: "admin"

        b.tvAdminName.text = nama
        b.etAdminNama.setText(nama)
        b.etAdminEmail.setText(email)

        val initial = nama.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"
        b.tvAdminAvatarInitial.text = initial

        b.tvStatusAkun.text = "Aktif"
        b.tvAdminRole.text = formatRole(role)
    }

    private fun tampilkanDataSession() {
        val nama = session.getName().ifEmpty { "Admin" }
        val email = session.getEmail().ifEmpty { "admin@gmail.com" }

        b.tvAdminName.text = nama
        b.etAdminNama.setText(nama)
        b.etAdminEmail.setText(email)

        val initial = nama.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"
        b.tvAdminAvatarInitial.text = initial

        b.tvStatusAkun.text = "Aktif"
        b.tvAdminRole.text = "Administrator"
    }

    private fun formatRole(role: String): String {
        return when (role.lowercase()) {
            "admin" -> "Administrator"
            "kasir" -> "Kasir"
            "customer" -> "Customer"
            else -> role
        }
    }
}