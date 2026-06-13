package prasetya.daffa.proyek_uas.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import prasetya.daffa.proyek_uas.adapter.KasirCustomOrderAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.KasirCustomOrderItem
import prasetya.daffa.proyek_uas.api.KasirCustomOrderResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.CustomOrderKasirFragmentBinding
import prasetya.daffa.proyek_uas.databinding.DialogUpdateCustomOrderBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomOrderKasirFragment : Fragment() {

    private var _b: CustomOrderKasirFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var customOrderAdapter: KasirCustomOrderAdapter
    private val listCustomOrder = mutableListOf<KasirCustomOrderItem>()

    private val statusList = listOf("pending", "diproses", "selesai")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = CustomOrderKasirFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadCustomOrder()
    }

    private fun setupRecyclerView() {
        customOrderAdapter = KasirCustomOrderAdapter(
            listCustomOrder = listCustomOrder,
            onUpdateClick = { item ->
                showDialogUpdate(item)
            }
        )

        b.rvCustomOrderKasir.layoutManager = LinearLayoutManager(requireContext())
        b.rvCustomOrderKasir.adapter = customOrderAdapter
    }

    private fun loadCustomOrder() {
        showLoading(true)

        ApiClient.instance.getKasirCustomOrder()
            .enqueue(object : Callback<KasirCustomOrderResponse> {
                override fun onResponse(
                    call: Call<KasirCustomOrderResponse>,
                    response: Response<KasirCustomOrderResponse>
                ) {
                    if (_b == null || !isAdded) return

                    showLoading(false)

                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        customOrderAdapter.setData(body.data)
                        updateEmptyState(body.data)
                        b.tvJumlahCustomOrder.text = "${body.data.size} Order"
                    } else {
                        updateEmptyState(emptyList())
                        b.tvJumlahCustomOrder.text = "0 Order"

                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal mengambil custom order",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<KasirCustomOrderResponse>, t: Throwable) {
                    if (_b == null || !isAdded) return

                    showLoading(false)
                    updateEmptyState(emptyList())
                    b.tvJumlahCustomOrder.text = "0 Order"

                    Toast.makeText(
                        requireContext(),
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun showDialogUpdate(item: KasirCustomOrderItem) {
        val dialogBinding = DialogUpdateCustomOrderBinding.inflate(layoutInflater)

        dialogBinding.tvTitleDialogCustom.text = "Update Custom Order"
        dialogBinding.tvInfoDialogCustom.text =
            "${item.jenisFurniture ?: "-"} - ${item.customer ?: "-"}"

        dialogBinding.etEstimasiHargaCustom.setText(
            item.estimasiHarga?.toString() ?: ""
        )

        val adapterStatus = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            statusList
        )

        dialogBinding.spinnerStatusCustom.adapter = adapterStatus

        val selectedStatus = item.status ?: "pending"
        val indexStatus = statusList.indexOf(selectedStatus)
        dialogBinding.spinnerStatusCustom.setSelection(
            if (indexStatus >= 0) indexStatus else 0
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Simpan", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            btnSimpan.setOnClickListener {
                val harga = dialogBinding.etEstimasiHargaCustom.text.toString().trim()
                val status = dialogBinding.spinnerStatusCustom.selectedItem.toString()

                if (harga.isEmpty()) {
                    dialogBinding.etEstimasiHargaCustom.error = "Harga wajib diisi"
                    return@setOnClickListener
                }

                val id = item.id
                if (id == null) {
                    Toast.makeText(
                        requireContext(),
                        "ID custom order tidak valid",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                updateCustomOrder(
                    id = id,
                    harga = harga,
                    status = status,
                    dialog = dialog
                )
            }
        }

        dialog.show()
    }

    private fun updateCustomOrder(
        id: Int,
        harga: String,
        status: String,
        dialog: AlertDialog
    ) {
        ApiClient.instance.updateKasirCustomOrder(id, harga, status)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        Toast.makeText(
                            requireContext(),
                            body.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                        loadCustomOrder()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal update custom order",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseDefault>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Koneksi gagal: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun updateEmptyState(data: List<KasirCustomOrderItem>) {
        if (data.isEmpty()) {
            b.layoutCustomOrderKosong.visibility = View.VISIBLE
            b.rvCustomOrderKasir.visibility = View.GONE
        } else {
            b.layoutCustomOrderKosong.visibility = View.GONE
            b.rvCustomOrderKasir.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _b?.progressCustomOrder?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}