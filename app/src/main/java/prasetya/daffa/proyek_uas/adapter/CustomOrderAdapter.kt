package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.R
import com.bumptech.glide.Glide

data class CustomOrder(
    val furnitureNama: String,
    val kayu: String,
    val ukuran: String,
    val harga: String,
    val status: String,
    val imageUrl: String = ""
)

class CustomOrderAdapter(
    private val list: List<CustomOrder>,
    private val onBayarClick: (CustomOrder) -> Unit
) : RecyclerView.Adapter<CustomOrderAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivFurnitureThumbnail)
        val tvNama: TextView = itemView.findViewById(R.id.tvFurnitureNama)
        val tvKayu: TextView = itemView.findViewById(R.id.tvKayu)
        val tvUkuran: TextView = itemView.findViewById(R.id.tvUkuran)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatusCustom)
        val btnBayar: TextView = itemView.findViewById(R.id.btnBayarSekarang)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_custom_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvNama.text = item.furnitureNama
        holder.tvKayu.text = item.kayu
        holder.tvUkuran.text = item.ukuran
        holder.tvHarga.text = formatHarga(item.harga)
        holder.tvStatus.text = item.status

        val gambarUrl = item.imageUrl
            .replace("\\/", "/")
            .replace(" ", "%20")

        Glide.with(holder.itemView.context)
            .load(gambarUrl.ifEmpty { null })
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(holder.ivThumbnail)

        holder.btnBayar.visibility =
            if (item.status.equals("Pending", ignoreCase = true)) View.VISIBLE else View.GONE

        holder.btnBayar.setOnClickListener {
            onBayarClick(item)
        }
    }

    override fun getItemCount() = list.size

    private fun formatHarga(harga: String): String {
        return try {
            val angka = harga.replace("[^0-9]".toRegex(), "").toLong()
            when {
                angka >= 1_000_000_000 -> "Rp ${angka / 1_000_000_000}M"
                angka >= 1_000_000 -> "Rp ${angka / 1_000_000}jt"
                angka >= 1_000 -> "Rp ${angka / 1_000}rb"
                else -> "Rp $angka"
            }
        } catch (e: Exception) {
            harga
        }
    }
}