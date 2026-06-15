package prasetya.daffa.proyek_uas.kasir

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import prasetya.daffa.proyek_uas.LoginActivity
import prasetya.daffa.proyek_uas.R
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

    private var currentUserId: Int   = 0
    private var currentName: String  = ""
    private var currentEmail: String = ""
    private var currentRole: String  = "kasir"

    private var isPasswordLamaVisible = false
    private var isPasswordBaruVisible = false
    private var isKonfirmasiVisible   = false

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
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        currentUserId = session.getUserId()

        setupView()
        setupButton()
        setupPasswordToggle()
        setupPasswordWarning()
        loadKasirFromDatabase()
    }

    private fun setupView() {
        b.etKasirUsername.isEnabled = false
        b.etKasirEmail.isEnabled    = false
        tampilkanDataSession()
    }

    private fun setupButton() {
        b.btnBackKasirTop.setOnClickListener { finish() }
        b.btnBackKasir.setOnClickListener    { finish() }
        b.btnPerbaruiPassword.setOnClickListener { validasiUpdatePassword() }
    }

    private fun setupPasswordToggle() {
        b.ivTogglePasswordLama.setOnClickListener {
            togglePassword(
                b.etKasirPasswordLama,
                b.ivTogglePasswordLama,
                ::isPasswordLamaVisible
            ) { isPasswordLamaVisible = it }
        }

        b.ivTogglePasswordBaru.setOnClickListener {
            togglePassword(
                b.etKasirPasswordBaru,
                b.ivTogglePasswordBaru,
                ::isPasswordBaruVisible
            ) { isPasswordBaruVisible = it }
        }

        b.ivToggleKonfirmasi.setOnClickListener {
            togglePassword(
                b.etKasirKonfirmasiPassword,
                b.ivToggleKonfirmasi,
                ::isKonfirmasiVisible
            ) { isKonfirmasiVisible = it }
        }
    }

    private fun togglePassword(
        editText: EditText,
        icon: ImageView,
        isVisible: () -> Boolean,
        setVisible: (Boolean) -> Unit
    ) {
        val newVisible = !isVisible()
        val animOut = AnimationUtils.loadAnimation(this, R.anim.anim_eye_close)
        val animIn  = AnimationUtils.loadAnimation(this, R.anim.anim_eye_open)

        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(a: Animation?) {}
            override fun onAnimationRepeat(a: Animation?) {}
            override fun onAnimationEnd(a: Animation?) {
                icon.setImageResource(
                    if (newVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                )
                icon.alpha = if (newVisible) 0.9f else 0.45f
                icon.startAnimation(animIn)
            }
        })

        icon.startAnimation(animOut)
        setVisible(newVisible)

        val cursor = editText.selectionEnd
        editText.transformationMethod = if (newVisible)
            HideReturnsTransformationMethod.getInstance()
        else
            PasswordTransformationMethod.getInstance()
        editText.setSelection(cursor)
    }

    private fun setupPasswordWarning() {
        b.etKasirKonfirmasiPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { checkPasswordMatch() }
        })

        b.etKasirPasswordBaru.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (b.etKasirKonfirmasiPassword.text.isNotEmpty()) checkPasswordMatch()
            }
        })
    }

    private fun checkPasswordMatch() {
        val baru    = b.etKasirPasswordBaru.text.toString()
        val konfirm = b.etKasirKonfirmasiPassword.text.toString()

        when {
            konfirm.isEmpty() -> {
                b.tvPasswordWarning.visibility = View.GONE
            }
            baru != konfirm -> {
                b.tvPasswordWarning.text = "⚠ Password tidak cocok"
                b.tvPasswordWarning.setTextColor(0xFFE53935.toInt())
                b.tvPasswordWarning.visibility = View.VISIBLE
            }
            else -> {
                b.tvPasswordWarning.text = "✓ Password cocok"
                b.tvPasswordWarning.setTextColor(0xFF3D6148.toInt())
                b.tvPasswordWarning.visibility = View.VISIBLE
            }
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
        currentName  = user.name  ?: "Kasir"
        currentEmail = user.email ?: "kasir@gmail.com"
        currentRole  = user.role  ?: "kasir"

        b.tvKasirName.text = currentName
        b.etKasirUsername.setText(currentName)
        b.etKasirEmail.setText(currentEmail)
        b.tvKasirAvatarInitial.text =
            currentName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"
    }

    private fun tampilkanDataSession() {
        currentName  = session.getName().ifEmpty  { "Kasir" }
        currentEmail = session.getEmail().ifEmpty { "kasir@gmail.com" }
        currentRole  = "kasir"

        b.tvKasirName.text = currentName
        b.etKasirUsername.setText(currentName)
        b.etKasirEmail.setText(currentEmail)
        b.tvKasirAvatarInitial.text =
            currentName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"
    }

    private fun validasiUpdatePassword() {
        val passwordLama   = b.etKasirPasswordLama.text.toString().trim()
        val passwordBaru   = b.etKasirPasswordBaru.text.toString().trim()
        val konfirmasiPass = b.etKasirKonfirmasiPassword.text.toString().trim()

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
            konfirmasiPass.isEmpty() -> {
                b.etKasirKonfirmasiPassword.error = "Konfirmasi password wajib diisi"
                b.etKasirKonfirmasiPassword.requestFocus()
            }
            passwordBaru != konfirmasiPass -> {
                b.tvPasswordWarning.text = "⚠ Password tidak cocok"
                b.tvPasswordWarning.setTextColor(0xFFE53935.toInt())
                b.tvPasswordWarning.visibility = View.VISIBLE
                b.etKasirKonfirmasiPassword.requestFocus()
            }
            else -> {
                updatePassword(passwordLama, passwordBaru, konfirmasiPass)
            }
        }
    }

    private fun updatePassword(
        passwordLama: String,
        passwordBaru: String,
        konfirmasiPassword: String
    ) {
        b.btnPerbaruiPassword.isEnabled = false
        b.btnPerbaruiPassword.text      = "Memproses..."

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
                b.btnPerbaruiPassword.text      = "Perbarui Password"

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
                    b.tvPasswordWarning.visibility = View.GONE

                    // Reset icon eye ke closed
                    isPasswordLamaVisible = false
                    isPasswordBaruVisible = false
                    isKonfirmasiVisible   = false
                    b.ivTogglePasswordLama.setImageResource(R.drawable.ic_eye_closed)
                    b.ivTogglePasswordBaru.setImageResource(R.drawable.ic_eye_closed)
                    b.ivToggleKonfirmasi.setImageResource(R.drawable.ic_eye_closed)
                    b.ivTogglePasswordLama.alpha = 0.45f
                    b.ivTogglePasswordBaru.alpha = 0.45f
                    b.ivToggleKonfirmasi.alpha   = 0.45f

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
                b.btnPerbaruiPassword.text      = "Perbarui Password"
                Toast.makeText(
                    this@KasirProfileActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}