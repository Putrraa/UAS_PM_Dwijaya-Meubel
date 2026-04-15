package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.databinding.HomeFragmentBinding

class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var b: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        b = HomeFragmentBinding.inflate(inflater, container, false)

        b.btnShop.setOnClickListener(this)

        return b.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnShop -> {

                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, ShopFragment())
                    .addToBackStack(null)
                    .commit()

                val mainAct = activity as? MainActivity
                mainAct?.setSelectedNav(R.id.shop)
            }
        }
    }
}