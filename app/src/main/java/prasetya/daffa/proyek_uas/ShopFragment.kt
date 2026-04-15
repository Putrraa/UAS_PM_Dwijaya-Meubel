package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment

import prasetya.daffa.proyek_uas.databinding.ShopFragmentBinding

class ShopFragment : Fragment() {

    private lateinit var b: ShopFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = ShopFragmentBinding.inflate(inflater, container, false)

        return b.root
    }
}