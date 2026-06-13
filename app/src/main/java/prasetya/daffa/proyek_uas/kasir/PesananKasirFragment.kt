package prasetya.daffa.proyek_uas.kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import prasetya.daffa.proyek_uas.adapter.PesananKasirAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.KasirPesananItem
import prasetya.daffa.proyek_uas.api.KasirPesananResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.PesananKasirFragmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PesananKasirFragment : Fragment() {

    private var _b: PesananKasirFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var pesananAdapter: PesananKasirAdapter
    private val listPesanan = mutableListOf<KasirPesananItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = PesananKasirFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadPesanan()
    }

    private fun setupRecyclerView() {
        pesananAdapter = PesananKasirAdapter(
            listPesanan = listPesanan,
            onUpdateStatus = { item, status ->
                val id = item.id

                if (id == null) {
                    Toast.makeText(
                        requireContext(),
                        "ID pesanan tidak valid",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    updateStatusPesanan(id, status)
                }
            }
        )

        b.rvPesananKasir.layoutManager = LinearLayoutManager(requireContext())
        b.rvPesananKasir.adapter = pesananAdapter
    }

    private fun loadPesanan() {
        showLoading(true)

        ApiClient.instance.getKasirPesanan()
            .enqueue(object : Callback<KasirPesananResponse> {
                override fun onResponse(
                    call: Call<KasirPesananResponse>,
                    response: Response<KasirPesananResponse>
                ) {
                    if (_b == null || !isAdded) return

                    showLoading(false)

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        pesananAdapter.setData(body.data)
                        updateEmptyState(body.data)
                        b.tvJumlahPesanan.text = "${body.data.size} Pesanan"
                    } else {
                        updateEmptyState(emptyList())
                        b.tvJumlahPesanan.text = "0 Pesanan"

                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal mengambil data pesanan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<KasirPesananResponse>, t: Throwable) {
                    if (_b == null || !isAdded) return

                    showLoading(false)
                    updateEmptyState(emptyList())
                    b.tvJumlahPesanan.text = "0 Pesanan"

                    Toast.makeText(
                        requireContext(),
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun updateStatusPesanan(id: Int, status: Int) {
        ApiClient.instance.updateStatusPesananKasir(id, status)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    if (_b == null || !isAdded) return

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        Toast.makeText(
                            requireContext(),
                            body.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        loadPesanan()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal update status pesanan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    if (_b == null || !isAdded) return

                    Toast.makeText(
                        requireContext(),
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun updateEmptyState(data: List<KasirPesananItem>) {
        if (data.isEmpty()) {
            b.layoutPesananKosong.visibility = View.VISIBLE
            b.rvPesananKasir.visibility = View.GONE
        } else {
            b.layoutPesananKosong.visibility = View.GONE
            b.rvPesananKasir.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _b?.progressPesanan?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}