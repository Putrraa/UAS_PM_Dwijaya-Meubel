package prasetya.daffa.proyek_uas.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.databinding.ActivityAdminBinding
import prasetya.daffa.proyek_uas.fragment.KelolaBarangFragment
//import prasetya.daffa.proyek_uas.fragment.LaporanAdminFragment
//import prasetya.daffa.proyek_uas.fragment.PenggunaAdminFragment

class AdminActivity : AppCompatActivity() {

    private lateinit var b: ActivityAdminBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbarAdmin)

        toggle = ActionBarDrawerToggle(
            this,
            b.drawerLayout,
            b.toolbarAdmin,
            R.string.open,
            R.string.close
        )

        b.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

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

//                R.id.laporan -> {
//                    replaceFragment(LaporanAdminFragment())
//                    supportActionBar?.title = "Laporan"
//                    true
//                }
//
//                R.id.pengguna -> {
//                    replaceFragment(PenggunaAdminFragment())
//                    supportActionBar?.title = "Data Pengguna"
//                    true
//                }

                else -> false
            }
        }

        b.navDrawerAdmin.setNavigationItemSelectedListener { item ->
            Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
            b.drawerLayout.closeDrawers()
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_admin, fragment)
            .commit()
    }
}