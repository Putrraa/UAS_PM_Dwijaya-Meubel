package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.databinding.ItemKategoriChipBinding

data class KategoriChipItem(
    val nama: String,
    val slug: String
)

class KategoriChipAdapter(
    private val listKategori: MutableList<KategoriChipItem>,
    private val onClick: (KategoriChipItem) -> Unit
) : RecyclerView.Adapter<KategoriChipAdapter.KategoriViewHolder>() {

    private var selectedSlug: String = "semua"

    inner class KategoriViewHolder(val b: ItemKategoriChipBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val binding = ItemKategoriChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KategoriViewHolder(binding)
    }

    override fun getItemCount(): Int = listKategori.size

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val item = listKategori[position]
        val context = holder.itemView.context
        val isActive = item.slug == selectedSlug

        holder.b.tvNamaKategoriChip.text = item.nama

        holder.b.tvNamaKategoriChip.setBackgroundResource(
            if (isActive) R.drawable.bg_nav_item_active
            else R.drawable.bg_nav_item_inactive
        )

        holder.b.tvNamaKategoriChip.setTextColor(
            ContextCompat.getColor(
                context,
                if (isActive) android.R.color.white else R.color.black
            )
        )

        holder.b.root.setOnClickListener {
            selectedSlug = item.slug
            notifyDataSetChanged()
            onClick(item)
        }
    }

    fun setData(data: List<KategoriChipItem>, selected: String) {
        selectedSlug = selected
        listKategori.clear()
        listKategori.addAll(data)
        notifyDataSetChanged()
    }

    fun setSelected(slug: String) {
        selectedSlug = slug
        notifyDataSetChanged()
    }
}