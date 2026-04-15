package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CheckoutFragment : Fragment(R.layout.checkout_fragment) {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvTotal: TextView

    private val cartList = mutableListOf<CartItem>()
    private lateinit var adapter: CartAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCart = view.findViewById(R.id.rvCart)
        tvTotal = view.findViewById(R.id.tvTotal)

        adapter = CartAdapter(cartList) {
            hitungTotal()
        }

        rvCart.layoutManager = LinearLayoutManager(requireContext())
        rvCart.adapter = adapter

        // Dummy
        tambahItem("Lemari A", 2500000)
        tambahItem("Sofa B", 3500000)
    }

    private fun tambahItem(nama: String, harga: Int) {
        cartList.add(CartItem(nama, harga, 1))
        adapter.notifyDataSetChanged()
        hitungTotal()
    }

    private fun hitungTotal() {
        var total = 0
        for (item in cartList) {
            total += item.price * item.qty
        }
        tvTotal.text = "Total: Rp $total"
    }
}