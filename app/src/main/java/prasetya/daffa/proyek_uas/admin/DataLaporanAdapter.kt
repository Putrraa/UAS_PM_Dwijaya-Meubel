package prasetya.daffa.proyek_uas.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.api.Laporan
import prasetya.daffa.proyek_uas.databinding.ItemLaporanBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class LaporanAdapter(
    private val listLaporan: MutableList<Laporan>
) : RecyclerView.Adapter<LaporanAdapter.LaporanViewHolder>() {

    inner class LaporanViewHolder(val b: ItemLaporanBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaporanViewHolder {
        val binding = ItemLaporanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LaporanViewHolder(binding)
    }

    override fun getItemCount(): Int = listLaporan.size

    override fun onBindViewHolder(holder: LaporanViewHolder, position: Int) {
        val item = listLaporan[position]

        holder.b.tvJenisPesanan.text = item.jenisPesanan ?: "-"
        holder.b.tvKodePesanan.text = item.kode ?: "-"
        holder.b.tvPembeli.text = item.pembeli ?: "-"
        holder.b.tvTanggal.text = formatTanggal(item.tanggal)
        holder.b.tvTotalHarga.text = formatRupiah(item.totalHarga ?: 0)
    }

    fun setData(data: List<Laporan>) {
        listLaporan.clear()
        listLaporan.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRupiah(value: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(value).replace(",00", "")
    }

    private fun formatTanggal(tanggal: String?): String {
        if (tanggal.isNullOrEmpty()) return "-"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(tanggal)

            if (date != null) outputFormat.format(date) else tanggal
        } catch (e: Exception) {
            tanggal
        }
    }
}