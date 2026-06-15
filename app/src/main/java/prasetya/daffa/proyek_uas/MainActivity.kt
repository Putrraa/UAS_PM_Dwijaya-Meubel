package prasetya.daffa.proyek_uas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.databinding.ActivityMainBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import prasetya.daffa.proyek_uas.admin.AdminActivity
import prasetya.daffa.proyek_uas.kasir.KasirActivity

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        if (session.isLogin()) {
            val intent = when (session.getRole().trim().lowercase()) {
                "admin" -> Intent(this, AdminActivity::class.java)
                "kasir" -> Intent(this, KasirActivity::class.java)
                else -> null
            }

            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }
        }
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
                    startActivity(Intent(this, KeranjangActivity::class.java))
                }

                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
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
        menu.findItem(R.id.menu_cart)?.isVisible = isLogin
        menu.findItem(R.id.menu_profile)?.isVisible = isLogin
        menu.findItem(R.id.menu_logout)?.isVisible = isLogin
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