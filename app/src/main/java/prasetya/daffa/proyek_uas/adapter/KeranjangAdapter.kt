package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.api.KeranjangItem
import prasetya.daffa.proyek_uas.databinding.ItemKeranjangBinding
import java.text.NumberFormat
import java.util.Locale

class KeranjangAdapter(
    private val listKeranjang: MutableList<KeranjangItem>,
    private val onTambah: (KeranjangItem) -> Unit,
    private val onKurang: (KeranjangItem) -> Unit,
    private val onHapus: (KeranjangItem) -> Unit
) : RecyclerView.Adapter<KeranjangAdapter.KeranjangViewHolder>() {

    inner class KeranjangViewHolder(val b: ItemKeranjangBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeranjangViewHolder {
        val binding = ItemKeranjangBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KeranjangViewHolder(binding)
    }

    override fun getItemCount(): Int = listKeranjang.size

    override fun onBindViewHolder(holder: KeranjangViewHolder, position: Int) {
        val item = listKeranjang[position]

        val harga = item.harga.toHargaInt()
        val jumlah = item.jumlah ?: 0
        val subtotal = harga * jumlah

        holder.b.tvNamaProduk.text = item.nama_barang ?: "-"
        holder.b.tvBrandProduk.text = "Dwijaya Meubel"
        holder.b.tvHargaSatuan.text = formatRupiah(harga)
        holder.b.tvJumlah.text = jumlah.toString()
        holder.b.tvSubtotal.text = formatRupiah(subtotal)

        val gambarUrl = item.gambar_url
            ?.replace("\\/", "/")
            ?.replace(" ", "%20")

        android.util.Log.d("GAMBAR_KERANJANG", "Load gambar: $gambarUrl")

        Glide.with(holder.itemView.context)
            .load(gambarUrl)
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(holder.b.imgProduk)

        holder.b.btnTambah.setOnClickListener {
            onTambah(item)
        }

        holder.b.btnKurang.setOnClickListener {
            if (jumlah > 1) {
                onKurang(item)
            }
        }

        holder.b.btnKurang.isEnabled = jumlah > 1
        holder.b.btnKurang.alpha = if (jumlah > 1) 1f else 0.35f

        holder.b.btnHapus.setOnClickListener {
            onHapus(item)
        }
    }

    fun setData(data: List<KeranjangItem>) {
        listKeranjang.clear()
        listKeranjang.addAll(data)
        notifyDataSetChanged()
    }

    private fun String?.toHargaInt(): Int {
        return this
            ?.replace("Rp", "")
            ?.replace(".", "")
            ?.replace(",", "")
            ?.replace(" ", "")
            ?.trim()
            ?.toIntOrNull() ?: 0
    }

    private fun formatRupiah(value: Int): String {
        val localeId = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeId)
        return formatter.format(value).replace(",00", "")
    }
}