package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import prasetya.daffa.proyek_uas.R
import prasetya.daffa.proyek_uas.api.Kategori
import prasetya.daffa.proyek_uas.databinding.ItemKategoriBinding

class KategoriAdapter(
    private val listKategori: MutableList<Kategori>,
    private val onClick: (Kategori) -> Unit
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    inner class KategoriViewHolder(val b: ItemKategoriBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val binding = ItemKategoriBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KategoriViewHolder(binding)
    }

    override fun getItemCount(): Int = listKategori.size

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val item = listKategori[position]

        holder.b.tvNamaKategori.text = item.nama_kategori

        Glide.with(holder.itemView.context)
            .load(item.gambar_url)
            .placeholder(R.drawable.home)
            .error(R.drawable.home)
            .into(holder.b.imgKategori)

        holder.b.cardKategori.setOnClickListener {
            onClick(item)
        }
    }

    fun setData(data: List<Kategori>) {
        listKategori.clear()
        listKategori.addAll(data)
        notifyDataSetChanged()
    }
}