package prasetya.daffa.proyek_uas.kasir

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.MainActivity
import prasetya.daffa.proyek_uas.kasir.PesananKasirFragment
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.databinding.ActivityKasirBinding
import prasetya.daffa.proyek_uas.fragment.CustomOrderKasirFragment
import prasetya.daffa.proyek_uas.helper.SessionManager

class KasirActivity : AppCompatActivity() {

    private lateinit var b: ActivityKasirBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityKasirBinding.inflate(layoutInflater)
        setContentView(b.root)
        enableEdgeToEdge()

        session = SessionManager(this)

        setupToolbar()
        setupBottomNavigation()

        if (savedInstanceState == null) {
            b.bottomNavKasir.selectedItemId = R.id.custom_order
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(b.toolbarKasir)
    }

    private fun setupBottomNavigation() {
        b.bottomNavKasir.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.custom_order -> {
                    replaceFragment(CustomOrderKasirFragment())
                    supportActionBar?.title = "Custom Order"
                    true
                }

                R.id.data_pesanan -> {
                    replaceFragment(PesananKasirFragment())
                    supportActionBar?.title = "Data Pesanan"
                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home_kasir, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, KasirProfileActivity::class.java))
                true
            }

            R.id.menu_logout -> {
                logoutUser()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        session.logout()

        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(b.frameKasir.id, fragment)
            .commit()
    }
}
