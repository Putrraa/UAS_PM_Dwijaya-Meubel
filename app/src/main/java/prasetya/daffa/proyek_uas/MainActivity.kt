package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

import prasetya.daffa.proyek_uas.databinding.ActivityMainBinding
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding
    lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)



        setSupportActionBar(b.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            b.drawerLayout,
            b.toolbar,
            0,
            0
        )
        b.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        b.navDrawer.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_login -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
                R.id.menu_cart -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_profile -> {
                    val intent = Intent(this, TambahBarangAdminActivity::class.java)
                    startActivity(intent)
                }

            }
            b.drawerLayout.closeDrawers()
            true
        }

        loadFragment(HomeFragment())

        b.bottomNav.setOnItemSelectedListener {
            var fragment: Fragment? = null

            when (it.itemId) {
                R.id.home -> fragment = HomeFragment()
                R.id.shop -> fragment = ShopFragment()
                R.id.about -> fragment = AboutFragment()
                R.id.contact -> fragment = ContactFragment()
                R.id.custom -> fragment = CustomFragment()
            }

            if (fragment != null) {
                loadFragment(fragment)
            }

            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    fun setSelectedNav(itemId: Int) {
        b.bottomNav.selectedItemId = itemId
    }
}