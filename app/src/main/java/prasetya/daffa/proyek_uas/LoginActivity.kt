package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import prasetya.daffa.proyek_uas.admin.AdminActivity
import prasetya.daffa.proyek_uas.kasir.KasirActivity
import prasetya.daffa.proyek_uas.helper.SessionManager


class LoginActivity : AppCompatActivity() {

    private lateinit var btnBack: LinearLayout
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: AppCompatButton
    private lateinit var tvDaftar: TextView
    private lateinit var ivTogglePassword: ImageView
    private lateinit var session: SessionManager

    // true  = password sedang terlihat
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        session = SessionManager(this)

        btnBack          = findViewById(R.id.btnBack)
        etEmail          = findViewById(R.id.etEmail)
        etPassword       = findViewById(R.id.etPassword)
        btnLogin         = findViewById(R.id.btnLogin)
        tvDaftar         = findViewById(R.id.tvDaftar)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)

        btnBack.setOnClickListener { finish() }

        tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        ivTogglePassword.setOnClickListener {
            togglePasswordVisibility(etPassword, ivTogglePassword, ::isPasswordVisible) {
                isPasswordVisible = it
            }
        }

        btnLogin.setOnClickListener { loginUser() }
    }

    /**
     * Fungsi generik untuk toggle visibility + animasi putar ikon mata.
     * [isVisible]  – getter lambda untuk state saat ini
     * [setVisible] – setter lambda untuk update state
     */
    private fun togglePasswordVisibility(
        editText: EditText,
        icon: ImageView,
        isVisible: () -> Boolean,
        setVisible: (Boolean) -> Unit
    ) {
        val newVisible = !isVisible()

        // Animasi keluar dulu, lalu ganti icon, lalu animasi masuk
        val animOut = AnimationUtils.loadAnimation(this, R.anim.anim_eye_close)
        val animIn  = AnimationUtils.loadAnimation(this, R.anim.anim_eye_open)

        animOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(a: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(a: android.view.animation.Animation?) {}
            override fun onAnimationEnd(a: android.view.animation.Animation?) {
                // Ganti icon setelah animasi keluar selesai
                icon.setImageResource(
                    if (newVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                )
                icon.alpha = if (newVisible) 0.9f else 0.5f
                icon.startAnimation(animIn)
            }
        })

        icon.startAnimation(animOut)

        setVisible(newVisible)

        val cursorPos = editText.selectionEnd
        editText.transformationMethod = if (newVisible)
            HideReturnsTransformationMethod.getInstance()
        else
            PasswordTransformationMethod.getInstance()

        editText.setSelection(cursorPos)
    }

    private fun loginUser() {
        val email    = etEmail.text.toString().trim()
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
        btnLogin.text      = "Memproses..."

        val url = "https://www.dwijayameubel.my.id/api/login"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                btnLogin.isEnabled = true
                btnLogin.text      = "Masuk"

                val json = JSONObject(response)
                val status = json.optBoolean("status")
                val message = json.optString("message", "Login gagal. Email atau password salah.")

                if (status) {
                    val userJson = json.optJSONObject("user")

                    if (userJson == null) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Data user tidak ditemukan",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val id = userJson.optInt("id")
                        val name = userJson.optString("name")
                        val userEmail = userJson.optString("email")
                        val role = userJson.optString("role")

                        session.saveUser(
                            id    = id,
                            name  = name,
                            email = userEmail,
                            role  = role
                        )

                        Toast.makeText(
                            this@LoginActivity,
                            "Login berhasil, selamat datang $name",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = when (role.trim().lowercase()) {
                            "admin"    -> Intent(this@LoginActivity, AdminActivity::class.java)
                            "customer" -> Intent(this@LoginActivity, MainActivity::class.java)
                            "kasir"    -> Intent(this@LoginActivity, KasirActivity::class.java)
                            else -> null
                        }

                        if (intent == null) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Role tidak dikenali: $role",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }

                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            { error ->
                btnLogin.isEnabled = true
                btnLogin.text      = "Masuk"

                val errorMessage = error.networkResponse?.data?.let {
                    try {
                        JSONObject(String(it)).optString("message", "Login gagal")
                    } catch (e: Exception) {
                        "Koneksi gagal"
                    }
                } ?: "Koneksi gagal: ${error.message}"

                Toast.makeText(
                    this@LoginActivity,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "email" to email,
                    "password" to password
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
