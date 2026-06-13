package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.api.KasirCustomOrderItem
import prasetya.daffa.proyek_uas.databinding.ItemCustomOrderKasirBinding

class KasirCustomOrderAdapter(
    private val listCustomOrder: MutableList<KasirCustomOrderItem>,
    private val onUpdateClick: (KasirCustomOrderItem) -> Unit
) : RecyclerView.Adapter<KasirCustomOrderAdapter.CustomOrderViewHolder>() {

    inner class CustomOrderViewHolder(val b: ItemCustomOrderKasirBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomOrderViewHolder {
        val binding = ItemCustomOrderKasirBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomOrderViewHolder(binding)
    }

    override fun getItemCount(): Int = listCustomOrder.size

    override fun onBindViewHolder(holder: CustomOrderViewHolder, position: Int) {
        val item = listCustomOrder[position]

        holder.b.tvFurnitureCustomKasir.text = item.jenisFurniture ?: "-"
        holder.b.tvCustomerCustomKasir.text = "Customer: ${item.customer ?: "-"}"
        holder.b.tvKayuCustomKasir.text = "Kayu: ${item.jenisKayu ?: "-"}"
        holder.b.tvUkuranCustomKasir.text = "Ukuran: ${item.ukuran ?: "-"}"
        holder.b.tvCatatanCustomKasir.text = "Catatan: ${item.catatan ?: "-"}"
        holder.b.tvHargaCustomKasir.text = item.harga ?: "-"
        holder.b.tvStatusCustomKasir.text = item.statusLabel ?: item.status ?: "-"

        val gambarUrl = item.gambarUrl
            ?.replace("\\/", "/")
            ?.replace(" ", "%20")

        Glide.with(holder.itemView.context)
            .load(gambarUrl)
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(holder.b.imgCustomOrderKasir)

        holder.b.btnUpdateCustomKasir.setOnClickListener {
            onUpdateClick(item)
        }
    }

    fun setData(data: List<KasirCustomOrderItem>) {
        listCustomOrder.clear()
        listCustomOrder.addAll(data)
        notifyDataSetChanged()
    }
}