package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.databinding.ItemProdukBinding
import java.text.NumberFormat
import java.util.Locale

class ProdukAdapter(
    private val listProduk: MutableList<Barang>,
    private val onClickDetail: (Barang) -> Unit,
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
        val stok = item.stok ?: 0
        holder.b.tvStokProduk.text = "Stok: $stok"

        if (stok > 0) {
            holder.b.btnAddCart.text = "ADD TO CART"
            holder.b.btnAddCart.isEnabled = true
            holder.b.btnAddCart.alpha = 1f
        } else {
            holder.b.btnAddCart.text = "STOK HABIS"
            holder.b.btnAddCart.isEnabled = false
            holder.b.btnAddCart.alpha = 0.6f
        }

        Glide.with(holder.itemView.context)
            .load(item.gambar_url)
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(holder.b.imgProduk)

        holder.b.layoutProduk.setOnClickListener {
            onClickDetail(item)
        }

        val showPopup = {
            showProdukPopup(holder, item, stok)
            true
        }

        holder.itemView.setOnLongClickListener { showPopup() }
        holder.b.cardProduk.setOnLongClickListener { showPopup() }
        holder.b.layoutProduk.setOnLongClickListener { showPopup() }

        holder.b.btnAddCart.setOnClickListener {
            onAddCart(item)
        }
    }

    private fun showProdukPopup(holder: ProdukViewHolder, item: Barang, stok: Int) {
        val context = holder.itemView.context
        val popup = PopupMenu(context, holder.b.cardProduk)

        popup.menuInflater.inflate(R.menu.menu_popup_produk, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.popup_detail_produk -> {
                    onClickDetail(item)
                    true
                }

                R.id.popup_tambah_keranjang -> {
                    if (stok > 0) {
                        onAddCart(item)
                    } else {
                        Toast.makeText(context, "Stok habis", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                else -> false
            }
        }

        popup.show()
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
