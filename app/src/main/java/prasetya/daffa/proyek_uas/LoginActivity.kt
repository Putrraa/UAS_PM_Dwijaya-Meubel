package prasetya.daffa.proyek_uas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import prasetya.daffa.proyek_uas.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var b: ActivityLoginBinding
    lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

//        // Mengubah warna status bar agar tidak ungu
//        window.statusBarColor = Color.parseColor("#F5F5F5")
//        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        db = DBOpenHelper(this)

        b.btnLogin.setOnClickListener(this)
        b.tvDaftar.setOnClickListener(this)
        b.btnBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.btnLogin -> {
                val email = b.etEmail.text.toString()
                val pass = b.etPassword.text.toString()

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Email & Password harus diisi", Toast.LENGTH_SHORT).show()
                    return
                }

                val role = db.checkLogin(email, pass)

                if (role != null) {
                    Toast.makeText(this, "Login sebagai $role", Toast.LENGTH_SHORT).show()

                    if (role == "admin") {
                        Toast.makeText(this, "Masuk Admin Page", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }

                } else {
                    Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.tvDaftar -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }
}