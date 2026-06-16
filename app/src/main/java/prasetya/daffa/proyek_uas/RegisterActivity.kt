package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CheckBox
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

class RegisterActivity : AppCompatActivity() {

    private lateinit var btnBack: LinearLayout
    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConPassword: EditText
    private lateinit var btnRegister: AppCompatButton
    private lateinit var tvMasuk: TextView
    private lateinit var ivTogglePassword: ImageView
    private lateinit var ivToggleConPassword: ImageView
    private lateinit var tvPasswordWarning: TextView  // ✅ tambah ini
    private lateinit var cbSetuju: CheckBox

    private var isPasswordVisible    = false
    private var isConPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        enableEdgeToEdge()
        btnBack             = findViewById(R.id.btnBack)
        etNama              = findViewById(R.id.etNama)
        etEmail             = findViewById(R.id.etEmail)
        etPassword          = findViewById(R.id.etPassword)
        etConPassword       = findViewById(R.id.etConPassword)
        btnRegister         = findViewById(R.id.btnRegister)
        tvMasuk             = findViewById(R.id.tvMasuk)
        ivTogglePassword    = findViewById(R.id.ivTogglePassword)
        ivToggleConPassword = findViewById(R.id.ivToggleConPassword)
        tvPasswordWarning   = findViewById(R.id.tvPasswordWarning)  // ✅ tambah ini
        cbSetuju            = findViewById(R.id.cbSetuju)

        btnBack.setOnClickListener { finish() }
        tvMasuk.setOnClickListener { finish() }

        ivTogglePassword.setOnClickListener {
            togglePasswordVisibility(etPassword, ivTogglePassword, ::isPasswordVisible) {
                isPasswordVisible = it
            }
        }

        ivToggleConPassword.setOnClickListener {
            togglePasswordVisibility(etConPassword, ivToggleConPassword, ::isConPasswordVisible) {
                isConPasswordVisible = it
            }
        }

        etConPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (etConPassword.text.isNotEmpty()) checkPasswordMatch()
            }
        })

        btnRegister.setOnClickListener { registerUser() }
    }


    private fun checkPasswordMatch() {
        val password    = etPassword.text.toString()
        val confirmPass = etConPassword.text.toString()

        when {
            confirmPass.isEmpty() -> {
                tvPasswordWarning.visibility = View.GONE
            }
            password != confirmPass -> {
                tvPasswordWarning.text       = "⚠ Password tidak cocok"
                tvPasswordWarning.setTextColor(0xFFE53935.toInt())
                tvPasswordWarning.visibility = View.VISIBLE
            }
            else -> {
                // Password cocok — tampilkan pesan hijau sebentar
                tvPasswordWarning.text       = "✓ Password cocok"
                tvPasswordWarning.setTextColor(0xFF3D6148.toInt())
                tvPasswordWarning.visibility = View.VISIBLE
            }
        }
    }

    private fun togglePasswordVisibility(
        editText: EditText,
        icon: ImageView,
        isVisible: () -> Boolean,
        setVisible: (Boolean) -> Unit
    ) {
        val newVisible = !isVisible()

        val animOut = AnimationUtils.loadAnimation(this, R.anim.anim_eye_close)
        val animIn  = AnimationUtils.loadAnimation(this, R.anim.anim_eye_open)

        animOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(a: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(a: android.view.animation.Animation?) {}
            override fun onAnimationEnd(a: android.view.animation.Animation?) {
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

    private fun registerUser() {
        val nama            = etNama.text.toString().trim()
        val email           = etEmail.text.toString().trim()
        val password        = etPassword.text.toString().trim()
        val confirmPassword = etConPassword.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama wajib diisi"; etNama.requestFocus(); return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email wajib diisi"; etEmail.requestFocus(); return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password wajib diisi"; etPassword.requestFocus(); return
        }
        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"; etPassword.requestFocus(); return
        }
        if (confirmPassword.isEmpty()) {
            etConPassword.error = "Konfirmasi password wajib diisi"; etConPassword.requestFocus(); return
        }
        if (password != confirmPassword) {
            tvPasswordWarning.text       = "⚠ Password tidak cocok"
            tvPasswordWarning.setTextColor(0xFFE53935.toInt())
            tvPasswordWarning.visibility = View.VISIBLE
            etConPassword.requestFocus()
            return
        }
        if (!cbSetuju.isChecked) {
            Toast.makeText(
                this,
                "Setujui syarat dan ketentuan terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            cbSetuju.requestFocus()
            return
        }

        btnRegister.isEnabled = false
        btnRegister.text      = "Memproses..."

        val url = "https://www.dwijayameubel.my.id/api/register"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                btnRegister.isEnabled = true
                btnRegister.text      = "Daftar"

                val json = JSONObject(response)
                val status = json.optBoolean("status")
                val message = json.optString("message", "Register gagal. Email mungkin sudah digunakan.")

                if (status) {
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
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            { error ->
                btnRegister.isEnabled = true
                btnRegister.text      = "Daftar"

                val errorMessage = error.networkResponse?.data?.let {
                    try {
                        JSONObject(String(it)).optString(
                            "message",
                            "Register gagal. Email mungkin sudah digunakan."
                        )
                    } catch (e: Exception) {
                        "Koneksi gagal"
                    }
                } ?: "Koneksi gagal: ${error.message}"

                Toast.makeText(
                    this@RegisterActivity,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "name" to nama,
                    "email" to email,
                    "password" to password
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
