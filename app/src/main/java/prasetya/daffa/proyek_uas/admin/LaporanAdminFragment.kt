package prasetya.daffa.proyek_uas.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import prasetya.daffa.proyek_uas.admin.DataLaporanAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Laporan
import prasetya.daffa.proyek_uas.api.LaporanResponse
import prasetya.daffa.proyek_uas.databinding.LaporanAdminFragmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class LaporanAdminFragment : Fragment() {

    private var _b: LaporanAdminFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var laporanAdapter: DataLaporanAdapter
    private val listLaporan = mutableListOf<Laporan>()

    private val baseUrl = "https://www.dwijayameubel.my.id/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = LaporanAdminFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadLaporan()
    }

    private fun setupRecyclerView() {
        laporanAdapter = DataLaporanAdapter(listLaporan)

        b.rvLaporan.layoutManager = LinearLayoutManager(requireContext())
        b.rvLaporan.adapter = laporanAdapter
    }


    private fun loadLaporan() {
        if (_b == null || !isAdded) return

        showLoading(true)

        ApiClient.instance.getLaporan().enqueue(object : Callback<LaporanResponse> {
            override fun onResponse(
                call: Call<LaporanResponse>,
                response: Response<LaporanResponse>
            ) {
                if (_b == null || !isAdded) return

                val ctx = context ?: return
                showLoading(false)

                val body = response.body()
                val data = body?.data.orEmpty()

                if (response.isSuccessful && body?.status == true) {
                    laporanAdapter.setData(data)
                    updateTotal(data)
                    updateEmptyState(data)
                } else {
                    Toast.makeText(
                        ctx,
                        body?.message ?: "Gagal mengambil data laporan",
                        Toast.LENGTH_SHORT
                    ).show()

                    laporanAdapter.setData(emptyList())
                    updateTotal(emptyList())
                    updateEmptyState(emptyList())
                }
            }

            override fun onFailure(call: Call<LaporanResponse>, t: Throwable) {
                if (_b == null || !isAdded) return

                val ctx = context ?: return
                showLoading(false)

                Toast.makeText(
                    ctx,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()

                laporanAdapter.setData(emptyList())
                updateTotal(emptyList())
                updateEmptyState(emptyList())
            }
        })
    }

    private fun updateTotal(data: List<Laporan>) {
        val grandTotal = data.sumOf { it.totalHarga ?: 0 }

        b.tvGrandTotal.text = formatRupiah(grandTotal)
        b.tvJumlahData.text = "${data.size} Data"
    }

    private fun updateEmptyState(data: List<Laporan>) {
        if (data.isEmpty()) {
            b.layoutLaporanKosong.visibility = View.VISIBLE
            b.rvLaporan.visibility = View.GONE
        } else {
            b.layoutLaporanKosong.visibility = View.GONE
            b.rvLaporan.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        b.progressLaporan.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun formatRupiah(value: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(value).replace(",00", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}