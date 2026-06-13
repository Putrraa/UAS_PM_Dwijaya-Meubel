package prasetya.daffa.proyek_uas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import prasetya.daffa.proyek_uas.api.Pengguna
import prasetya.daffa.proyek_uas.databinding.ItemPenggunaBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PenggunaAdapter(
    private val listPengguna: MutableList<Pengguna>,
    private val currentUserId: Int,
    private val onEdit: (Pengguna) -> Unit,
    private val onDelete: (Pengguna) -> Unit
) : RecyclerView.Adapter<PenggunaAdapter.PenggunaViewHolder>() {

    inner class PenggunaViewHolder(val b: ItemPenggunaBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenggunaViewHolder {
        val binding = ItemPenggunaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PenggunaViewHolder(binding)
    }

    override fun getItemCount(): Int = listPengguna.size

    override fun onBindViewHolder(holder: PenggunaViewHolder, position: Int) {
        val item = listPengguna[position]

        val nama = item.name ?: "-"
        val email = item.email ?: "-"
        val role = item.role ?: "customer"

        holder.b.tvInitialPengguna.text =
            nama.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"

        holder.b.tvNamaPengguna.text = nama
        holder.b.tvEmailPengguna.text = email
        holder.b.tvRolePengguna.text = formatRole(role)
        holder.b.tvTanggalPengguna.text = "Terdaftar: ${formatTanggal(item.createdAt)}"

        holder.b.btnEditPengguna.setOnClickListener {
            onEdit(item)
        }

        if (item.id == currentUserId) {
            holder.b.btnHapusPengguna.visibility = View.GONE
        } else {
            holder.b.btnHapusPengguna.visibility = View.VISIBLE
            holder.b.btnHapusPengguna.setOnClickListener {
                onDelete(item)
            }
        }
    }

    fun setData(data: List<Pengguna>) {
        listPengguna.clear()
        listPengguna.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRole(role: String): String {
        return when (role.lowercase()) {
            "admin" -> "Admin"
            "kasir" -> "Kasir"
            else -> "Customer"
        }
    }

    private fun formatTanggal(tanggal: String?): String {
        if (tanggal.isNullOrEmpty()) return "-"

        return try {
            val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val date = input.parse(tanggal)
            if (date != null) output.format(date) else tanggal
        } catch (e: Exception) {
            try {
                val inputIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                val output = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                val date = inputIso.parse(tanggal)
                if (date != null) output.format(date) else tanggal
            } catch (e: Exception) {
                tanggal.take(10)
            }
        }
    }
}