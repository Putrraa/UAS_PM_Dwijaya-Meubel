package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.databinding.ItemDataBarangBinding
import java.text.NumberFormat
import java.util.Locale

class DataBarangAdapter(
    private val data: MutableList<Barang>,
    private val onEdit: (Barang) -> Unit,
    private val onHapus: (Barang) -> Unit
) : RecyclerView.Adapter<DataBarangAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemDataBarangBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDataBarangBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.b.tvNamaBarang.text = item.nama_barang ?: "-"
        holder.b.tvKategori.text = "Kategori: ${item.kategori?.nama_kategori ?: "-"}"
        holder.b.tvBahan.text = "Bahan: ${item.bahan?.nama_bahan ?: "-"}"
        holder.b.tvHarga.text = formatRupiah(item.harga)
        holder.b.tvStok.text = "Stok: ${item.stok ?: 0}"

        Glide.with(holder.itemView.context)
            .load(item.gambar_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(holder.b.imgBarang)

        holder.b.btnEdit.setOnClickListener {
            onEdit(item)
        }

        holder.b.btnHapus.setOnClickListener {
            onHapus(item)
        }
    }

    fun setData(newData: List<Barang>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    private fun formatRupiah(value: String?): String {
        val angka = value?.toDoubleOrNull() ?: 0.0
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(angka).replace(",00", "")
    }
}