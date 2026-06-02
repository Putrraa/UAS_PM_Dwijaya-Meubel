package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import prasetya.daffa.proyek_uas.databinding.ActivityLoginBinding

import prasetya.daffa.proyek_uas.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var b : ActivityRegisterBinding
    lateinit var db : DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)

        b.btnLogin.setOnClickListener(this)
        b.btnRegister.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnLogin -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.btnRegister -> {

            }
        }


    }
}