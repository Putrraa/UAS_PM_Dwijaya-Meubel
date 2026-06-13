package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import prasetya.daffa.proyek_uas.admin.AdminActivity
import prasetya.daffa.proyek_uas.kasir.KasirActivity
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.AuthResponse
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var btnBack: LinearLayout
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: AppCompatButton
    private lateinit var tvDaftar: TextView
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        session = SessionManager(this)

        btnBack = findViewById(R.id.btnBack)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvDaftar = findViewById(R.id.tvDaftar)

        btnBack.setOnClickListener {
            finish()
        }

        tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email wajib diisi"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password wajib diisi"
            etPassword.requestFocus()
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Memproses..."

        ApiClient.instance.login(email, password).enqueue(object : Callback<AuthResponse> {

            override fun onResponse(
                call: Call<AuthResponse>,
                response: Response<AuthResponse>
            ) {
                btnLogin.isEnabled = true
                btnLogin.text = "Masuk"

                val body = response.body()

                if (response.isSuccessful && body?.status == true && body.user != null) {
                    val user = body.user

                    session.saveUser(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        role = user.role
                    )

                    Toast.makeText(
                        this@LoginActivity,
                        "Login berhasil, selamat datang ${user.name}",
                        Toast.LENGTH_LONG
                    ).show()

                    val role = user.role.trim().lowercase()

                    val intent = when (role) {
                        "admin" -> Intent(this@LoginActivity, AdminActivity::class.java)
                        "customer" -> Intent(this@LoginActivity, MainActivity::class.java)
                        "kasir" -> Intent(this@LoginActivity, KasirActivity::class.java)
                        // Kalau nanti sudah ada KasirActivity:


                        else -> {
                            Toast.makeText(
                                this@LoginActivity,
                                "Role tidak dikenali: ${user.role}",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }
                    }

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        body?.message ?: "Login gagal. Email atau password salah.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnLogin.isEnabled = true
                btnLogin.text = "Masuk"

                Toast.makeText(
                    this@LoginActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}