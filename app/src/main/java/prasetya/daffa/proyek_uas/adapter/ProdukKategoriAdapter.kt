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

class ProdukKategoriAdapter(
    private val listProduk: MutableList<Barang>,
    private val onClickProduk: (Barang) -> Unit
) : RecyclerView.Adapter<ProdukKategoriAdapter.ProdukViewHolder>() {

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

        val nama = item.nama_barang ?: "-"
        val harga = parseHarga(item.harga)
        val stok = item.stok ?: 0
        val gambarUrl = item.gambar_url?.replace("\\/", "/")?.replace(" ", "%20")

        holder.b.tvNamaProduk.text = nama.uppercase()
        holder.b.tvHargaProduk.text = formatRupiah(harga)

        if (stok > 0) {
            holder.b.tvStokProduk.text = "Stok $stok"
            holder.b.btnAddCart.text = "ADD TO CART"
            holder.b.btnAddCart.isEnabled = true
            holder.b.btnAddCart.alpha = 1f
        } else {
            holder.b.tvStokProduk.text = "Habis"
            holder.b.btnAddCart.text = "STOK HABIS"
            holder.b.btnAddCart.isEnabled = true
            holder.b.btnAddCart.alpha = 0.7f
        }

        Glide.with(holder.itemView.context)
            .load(gambarUrl)
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(holder.b.imgProduk)

        holder.b.layoutProduk.setOnClickListener {
            onClickProduk(item)
        }

        holder.b.btnAddCart.setOnClickListener {
            onClickProduk(item)
        }
    }

    fun setData(data: List<Barang>) {
        listProduk.clear()
        listProduk.addAll(data)
        notifyDataSetChanged()
    }

    private fun parseHarga(value: String?): Int {
        if (value.isNullOrEmpty()) return 0

        return value
            .replace("Rp", "")
            .replace(".", "")
            .replace(",", "")
            .trim()
            .toIntOrNull() ?: 0
    }

    private fun formatRupiah(value: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(value).replace(",00", "")
    }
}