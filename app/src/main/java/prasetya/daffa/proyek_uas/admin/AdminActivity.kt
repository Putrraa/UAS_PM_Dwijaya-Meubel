package prasetya.daffa.proyek_uas.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.MainActivity
import prasetya.daffa.proyek_uas.admin.AdminProfileActivity
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.databinding.ActivityAdminBinding
import prasetya.daffa.proyek_uas.fragment.KelolaBarangFragment
import prasetya.daffa.proyek_uas.helper.SessionManager
import prasetya.daffa.proyek_uas.fragment.LaporanAdminFragment
import prasetya.daffa.proyek_uas.fragment.PenggunaAdminFragment

class AdminActivity : AppCompatActivity() {

    private lateinit var b: ActivityAdminBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(b.root)
        enableEdgeToEdge()

        session = SessionManager(this)

        setSupportActionBar(b.toolbarAdmin)

        if (savedInstanceState == null) {
            replaceFragment(KelolaBarangFragment())
            b.bottomNavAdmin.selectedItemId = R.id.kelola
            supportActionBar?.title = "Kelola Barang"
        }

        b.bottomNavAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.kelola -> {
                    replaceFragment(KelolaBarangFragment())
                    supportActionBar?.title = "Kelola Barang"
                    true
                }

                R.id.laporan -> {
                    replaceFragment(LaporanAdminFragment())
                    supportActionBar?.title = "Laporan"
                    true
                }
                R.id.pengguna -> {
                    replaceFragment(PenggunaAdminFragment())
                    supportActionBar?.title = "Data Pengguna"
                    true
                }

                else -> false
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home_admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, AdminProfileActivity::class.java))
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
            .replace(R.id.frame_admin, fragment)
            .commit()
    }
}
