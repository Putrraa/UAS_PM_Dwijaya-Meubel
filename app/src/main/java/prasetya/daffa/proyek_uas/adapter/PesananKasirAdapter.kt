package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.api.KasirPesananItem
import prasetya.daffa.proyek_uas.databinding.ItemPesananKasirBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PesananKasirAdapter(
    private val listPesanan: MutableList<KasirPesananItem>,
    private val onUpdateStatus: (KasirPesananItem, Int) -> Unit
) : RecyclerView.Adapter<PesananKasirAdapter.PesananViewHolder>() {

    private val statusLabelList = listOf("Diproses", "Dikirim", "Selesai")

    inner class PesananViewHolder(val b: ItemPesananKasirBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val binding = ItemPesananKasirBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PesananViewHolder(binding)
    }

    override fun getItemCount(): Int = listPesanan.size

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val item = listPesanan[position]

        holder.b.tvKodePesananKasir.text = item.kode ?: "-"
        holder.b.tvStatusPesananKasir.text = item.statusLabel ?: formatStatus(item.status)
        holder.b.tvTanggalPesananKasir.text = formatTanggal(item.tanggal)

        holder.b.tvPenerimaPesananKasir.text = item.namaPenerima ?: "-"
        holder.b.tvTeleponPesananKasir.text = item.noTelepon ?: "-"

        val alamatLengkap = buildString {
            append("Alamat: ")
            append(item.alamat ?: "-")

            if (!item.kota.isNullOrEmpty()) {
                append("\nKota: ${item.kota}")
            }

            if (!item.kodePos.isNullOrEmpty()) {
                append(" (${item.kodePos})")
            }
        }

        holder.b.tvAlamatPesananKasir.text = alamatLengkap
        holder.b.tvTotalPesananKasir.text = item.total ?: "Rp 0"
        holder.b.tvMetodeBayarPesananKasir.text = item.metodeLabel ?: "-"

        val spinnerAdapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_dropdown_item,
            statusLabelList
        )

        holder.b.spinnerStatusPesananKasir.adapter = spinnerAdapter
        holder.b.spinnerStatusPesananKasir.setSelection(statusToIndex(item.status ?: 1))

        holder.b.btnUpdateStatusPesananKasir.setOnClickListener {
            val selectedIndex = holder.b.spinnerStatusPesananKasir.selectedItemPosition
            val statusValue = indexToStatus(selectedIndex)
            onUpdateStatus(item, statusValue)
        }
    }

    fun setData(data: List<KasirPesananItem>) {
        listPesanan.clear()
        listPesanan.addAll(data)
        notifyDataSetChanged()
    }

    private fun statusToIndex(status: Int): Int {
        return when (status) {
            1 -> 0
            2 -> 1
            3 -> 2
            else -> 0
        }
    }

    private fun indexToStatus(index: Int): Int {
        return when (index) {
            0 -> 1
            1 -> 2
            2 -> 3
            else -> 1
        }
    }

    private fun formatStatus(status: Int?): String {
        return when (status) {
            1 -> "Diproses"
            2 -> "Dikirim"
            3 -> "Selesai"
            else -> "Diproses"
        }
    }

    private fun formatTanggal(tanggal: String?): String {
        if (tanggal.isNullOrEmpty()) return "-"

        return try {
            val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val date = input.parse(tanggal)
            if (date != null) output.format(date) else tanggal
        } catch (e: Exception) {
            tanggal
        }
    }
}