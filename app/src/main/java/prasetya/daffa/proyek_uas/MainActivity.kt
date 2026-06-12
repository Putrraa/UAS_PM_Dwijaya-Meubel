package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.databinding.ActivityMainBinding
import prasetya.daffa.proyek_uas.helper.SessionManager

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        session = SessionManager(this)

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

        updateMenuLogin()

        b.navDrawer.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.menu_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }

                R.id.menu_cart -> {
                    if (session.isLogin()) {
                        // Ganti ke CartActivity kalau sudah ada
                        // startActivity(Intent(this, CartActivity::class.java))

                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                }

                R.id.menu_logout -> {

                }

                R.id.menu_profile -> {
                    if (session.isLogin()) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
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

    override fun onResume() {
        super.onResume()
        updateMenuLogin()
    }

    private fun updateMenuLogin() {
        val menu = b.navDrawer.menu

        val isLogin = session.isLogin()

        menu.findItem(R.id.menu_login)?.isVisible = !isLogin

        // Kalau kamu punya menu register di drawer, aktifkan ini:
        // menu.findItem(R.id.menu_register)?.isVisible = !isLogin

        menu.findItem(R.id.menu_profile)?.isVisible = isLogin
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