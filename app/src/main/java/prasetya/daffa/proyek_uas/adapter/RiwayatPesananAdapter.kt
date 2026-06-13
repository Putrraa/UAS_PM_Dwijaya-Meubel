package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.R

data class RiwayatPesanan(
    val noPesanan: String,
    val tanggal: String,
    val total: String,
    val status: String
)

class RiwayatPesananAdapter(
    private val list: List<RiwayatPesanan>,
    private val onDetailClick: (RiwayatPesanan) -> Unit
) : RecyclerView.Adapter<RiwayatPesananAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoPesanan: TextView = itemView.findViewById(R.id.tvNoPesanan)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnDetail: Button = itemView.findViewById(R.id.btnDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_pesanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNoPesanan.text = item.noPesanan
        holder.tvTanggal.text = item.tanggal
        holder.tvTotal.text = item.total
        holder.tvStatus.text = item.status
        holder.btnDetail.setOnClickListener { onDetailClick(item) }
    }

    override fun getItemCount() = list.size
}