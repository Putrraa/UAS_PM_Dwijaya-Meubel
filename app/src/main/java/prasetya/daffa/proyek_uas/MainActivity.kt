package prasetya.daffa.proyek_uas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

import prasetya.daffa.proyek_uas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(b.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                R.id.menu_login -> loadFragment(LoginFragment())
                R.id.menu_cart -> loadFragment(CheckoutFragment())
                R.id.menu_profile -> loadFragment(AboutFragment())
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