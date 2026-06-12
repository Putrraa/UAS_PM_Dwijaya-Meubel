package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.AuthResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnBack: LinearLayout
    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConPassword: EditText
    private lateinit var btnRegister: AppCompatButton
    private lateinit var tvMasuk: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnBack = findViewById(R.id.btnBack)
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConPassword = findViewById(R.id.etConPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvMasuk = findViewById(R.id.tvMasuk)

        btnBack.setOnClickListener {
            finish()
        }

        tvMasuk.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConPassword.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama wajib diisi"
            etNama.requestFocus()
            return
        }

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

        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            etPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            etConPassword.error = "Konfirmasi password wajib diisi"
            etConPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConPassword.error = "Konfirmasi password tidak sama"
            etConPassword.requestFocus()
            return
        }

        btnRegister.isEnabled = false
        btnRegister.text = "Memproses..."

        ApiClient.instance.register(nama, email, password).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                btnRegister.isEnabled = true
                btnRegister.text = "Daftar"

                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Register berhasil, silakan masuk",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        body?.message ?: "Register gagal. Email mungkin sudah digunakan.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                btnRegister.isEnabled = true
                btnRegister.text = "Daftar"

                Toast.makeText(
                    this@RegisterActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}