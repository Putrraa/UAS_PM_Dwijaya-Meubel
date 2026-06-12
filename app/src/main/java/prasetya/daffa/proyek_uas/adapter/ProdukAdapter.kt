package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.databinding.ItemProdukBinding
import java.text.NumberFormat
import java.util.Locale

class ProdukAdapter(
    private val listProduk: MutableList<Barang>,
    private val onAddCart: (Barang) -> Unit
) : RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    inner class ProdukViewHolder(val b: ItemProdukBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val binding = ItemProdukBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProdukViewHolder(binding)
    }

    override fun getItemCount(): Int = listProduk.size

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val item = listProduk[position]

        holder.b.tvNamaProduk.text = item.nama_barang ?: "-"
        holder.b.tvHargaProduk.text = formatRupiah(item.harga)
        holder.b.tvStokProduk.text = "Stok: ${item.stok ?: 0}"

        Glide.with(holder.itemView.context)
            .load(item.gambar_url)
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(holder.b.imgProduk)

        holder.b.btnAddCart.setOnClickListener {
            onAddCart(item)
        }
    }

    fun setData(data: List<Barang>) {
        listProduk.clear()
        listProduk.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRupiah(value: String?): String {
        val angka = value
            ?.replace("Rp", "")
            ?.replace(".", "")
            ?.replace(",", "")
            ?.trim()
            ?.toIntOrNull() ?: 0

        val localeId = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeId)

        return formatter.format(angka).replace(",00", "")
    }
}