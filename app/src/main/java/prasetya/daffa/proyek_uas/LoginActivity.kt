package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import prasetya.daffa.proyek_uas.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var b : ActivityLoginBinding
    lateinit var db : DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)

        b.btnLogin.setOnClickListener(this)
        b.btnRegister.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
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
                        // TODO: pindah ke AdminActivity
                    } else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }

                } else {
                    Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btnRegister -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
        }


    }
}