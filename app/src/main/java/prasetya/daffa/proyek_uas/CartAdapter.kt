package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class CartAdapter(
    private val list: MutableList<CartItem>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvQty: TextView = view.findViewById(R.id.tvQty)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvName.text = item.name
        holder.tvPrice.text = "Rp ${item.price}"
        holder.tvQty.text = item.qty.toString()

        holder.btnPlus.setOnClickListener {
            item.qty++
            notifyItemChanged(position)
            onChanged()
        }

        holder.btnMinus.setOnClickListener {
            if (item.qty > 1) {
                item.qty--
                notifyItemChanged(position)
                onChanged()
            }
        }

        holder.btnDelete.setOnClickListener {
            list.removeAt(position)
            notifyItemRemoved(position)
            onChanged()
        }
    }
}