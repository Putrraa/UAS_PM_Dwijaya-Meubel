package prasetya.daffa.proyek_uas.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import prasetya.daffa.proyek_uas.adapter.DataBarangAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Barang
import prasetya.daffa.proyek_uas.api.BarangListResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.KelolaBarangFragmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KelolaBarangFragment : Fragment() {

    private var _b: KelolaBarangFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var adapter: DataBarangAdapter
    private val semuaBarang = mutableListOf<Barang>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = KelolaBarangFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = DataBarangAdapter(
            mutableListOf(),
            onEdit = { barang ->
                Toast.makeText(
                    requireContext(),
                    "Edit ${barang.nama_barang}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onHapus = { barang ->
                konfirmasiHapus(barang)
            }
        )

        b.rvBarang.layoutManager = LinearLayoutManager(requireContext())
        b.rvBarang.adapter = adapter

        b.btnTambahBarang.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Form tambah barang nanti kita buat di dialog",
                Toast.LENGTH_SHORT
            ).show()
        }

        b.btnCariBarang.setOnClickListener {
            cariBarang()
        }

        b.btnResetCari.setOnClickListener {
            b.edtCariBarang.setText("")
            adapter.setData(semuaBarang)
            cekDataKosong(semuaBarang)
        }

        loadBarang()
    }

    private fun loadBarang() {
        b.progressBar.visibility = View.VISIBLE
        b.tvKosong.visibility = View.GONE

        ApiClient.instance.getBarang().enqueue(object : Callback<BarangListResponse> {
            override fun onResponse(
                call: Call<BarangListResponse>,
                response: Response<BarangListResponse>
            ) {
                b.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data ?: emptyList()

                    semuaBarang.clear()
                    semuaBarang.addAll(data)

                    adapter.setData(semuaBarang)
                    cekDataKosong(semuaBarang)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data barang",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<BarangListResponse>, t: Throwable) {
                b.progressBar.visibility = View.GONE

                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun cariBarang() {
        val keyword = b.edtCariBarang.text.toString().trim().lowercase()

        if (keyword.isEmpty()) {
            adapter.setData(semuaBarang)
            cekDataKosong(semuaBarang)
            return
        }

        val hasil = semuaBarang.filter {
            it.nama_barang?.lowercase()?.contains(keyword) == true
        }

        adapter.setData(hasil)
        cekDataKosong(hasil)
    }

    private fun konfirmasiHapus(barang: Barang) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Barang")
            .setMessage("Yakin ingin menghapus ${barang.nama_barang}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusBarang(barang)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusBarang(barang: Barang) {
        val id = barang.id ?: return

        b.progressBar.visibility = View.VISIBLE

        ApiClient.instance.deleteBarang(id).enqueue(object : Callback<ResponseDefault> {
            override fun onResponse(
                call: Call<ResponseDefault>,
                response: Response<ResponseDefault>
            ) {
                b.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(
                        requireContext(),
                        "Barang berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadBarang()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menghapus barang",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                b.progressBar.visibility = View.GONE

                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun cekDataKosong(data: List<Barang>) {
        b.tvKosong.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}