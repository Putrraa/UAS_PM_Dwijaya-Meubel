package prasetya.daffa.proyek_uas.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.KeranjangActivity
import prasetya.daffa.proyek_uas.LoginActivity
import prasetya.daffa.proyek_uas.MainActivity
import prasetya.daffa.proyek_uas.ProfileActivity
import prasetya.daffa.proyek_uas.kasir.PesananKasirFragment
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.databinding.ActivityKasirBinding
import prasetya.daffa.proyek_uas.fragment.CustomOrderKasirFragment
import prasetya.daffa.proyek_uas.helper.SessionManager
import kotlin.jvm.java

class KasirActivity : AppCompatActivity() {

    private lateinit var b: ActivityKasirBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityKasirBinding.inflate(layoutInflater)
        setContentView(b.root)
        enableEdgeToEdge()

        session = SessionManager(this)

        setupToolbar()
        setupBottomNavigation()
        setupNavDrawer()
        setupBackPressed()

        if (savedInstanceState == null) {
            b.bottomNavKasir.selectedItemId = R.id.custom_order
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(b.toolbarKasir)

        toggle = ActionBarDrawerToggle(
            this,
            b.drawerLayout,
            b.toolbarKasir,
            R.string.open,
            R.string.close
        )

        b.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
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

    private fun setupNavDrawer() {
        b.navDrawerKasir.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_profile -> {
                    startActivity(Intent(this, KasirProfileActivity::class.java))
                }

                R.id.menu_logout -> {
                    session.logout()

                    Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }

            b.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (b.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    b.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(b.frameKasir.id, fragment)
            .commit()
    }
}