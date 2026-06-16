package prasetya.daffa.proyek_uas.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import prasetya.daffa.proyek_uas.adapter.PenggunaAdapter
import prasetya.daffa.proyek_uas.api.ApiClient
import prasetya.daffa.proyek_uas.api.Pengguna
import prasetya.daffa.proyek_uas.api.PenggunaResponse
import prasetya.daffa.proyek_uas.api.ResponseDefault
import prasetya.daffa.proyek_uas.databinding.DialogPenggunaBinding
import prasetya.daffa.proyek_uas.databinding.PenggunaAdminFragmentBinding
import prasetya.daffa.proyek_uas.helper.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PenggunaAdminFragment : Fragment() {

    private var _b: PenggunaAdminFragmentBinding? = null
    private val b get() = _b!!

    private lateinit var session: SessionManager
    private lateinit var penggunaAdapter: PenggunaAdapter
    private val listPengguna = mutableListOf<Pengguna>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = PenggunaAdminFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())

        setupRecyclerView()
        setupButton()
        loadPengguna()
    }

    private fun setupRecyclerView() {
        penggunaAdapter = PenggunaAdapter(
            listPengguna = listPengguna,
            currentUserId = session.getUserId(),
            onEdit = { pengguna ->
                showDialogPengguna(isEdit = true, pengguna = pengguna)
            },
            onDelete = { pengguna ->
                confirmDelete(pengguna)
            }
        )

        b.rvPengguna.layoutManager = LinearLayoutManager(requireContext())
        b.rvPengguna.adapter = penggunaAdapter
    }

    private fun setupButton() {
        b.btnTambahPengguna.setOnClickListener {
            showDialogPengguna(isEdit = false, pengguna = null)
        }
    }

    private fun loadPengguna() {
        if (_b == null || !isAdded) return

        showLoading(true)

        ApiClient.instance.getPengguna().enqueue(object : Callback<PenggunaResponse> {
            override fun onResponse(
                call: Call<PenggunaResponse>,
                response: Response<PenggunaResponse>
            ) {
                if (_b == null || !isAdded) return

                val ctx = context ?: return
                showLoading(false)

                val body = response.body()
                val data = body?.data.orEmpty()

                if (response.isSuccessful && body?.status == true) {
                    penggunaAdapter.setData(data)
                    updateEmptyState(data)
                    b.tvJumlahPengguna.text = "${data.size} Pengguna"
                } else {
                    penggunaAdapter.setData(emptyList())
                    updateEmptyState(emptyList())
                    b.tvJumlahPengguna.text = "0 Pengguna"

                    Toast.makeText(
                        ctx,
                        body?.message ?: "Gagal mengambil data pengguna",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PenggunaResponse>, t: Throwable) {
                if (_b == null || !isAdded) return

                val ctx = context ?: return
                showLoading(false)

                penggunaAdapter.setData(emptyList())
                updateEmptyState(emptyList())
                b.tvJumlahPengguna.text = "0 Pengguna"

                Toast.makeText(
                    ctx,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun showDialogPengguna(isEdit: Boolean, pengguna: Pengguna?) {
        val dialogBinding = DialogPenggunaBinding.inflate(layoutInflater)

        dialogBinding.tvTitleDialogPengguna.text =
            if (isEdit) "Edit Pengguna" else "Tambah Pengguna"

        if (isEdit && pengguna != null) {
            dialogBinding.etNamaPengguna.setText(pengguna.name ?: "")
            dialogBinding.etEmailPengguna.setText(pengguna.email ?: "")
            dialogBinding.etPasswordPengguna.hint = "Password baru (kosongkan jika tidak diganti)"
            setSelectedRole(dialogBinding, pengguna.role ?: "customer")
        } else {
            setSelectedRole(dialogBinding, "customer")
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setNegativeButton("Batal", null)
            .setPositiveButton(if (isEdit) "Simpan" else "Tambah", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            btnSimpan.setOnClickListener {
                val name = dialogBinding.etNamaPengguna.text.toString().trim()
                val email = dialogBinding.etEmailPengguna.text.toString().trim()
                val password = dialogBinding.etPasswordPengguna.text.toString().trim()
                val role = getSelectedRole(dialogBinding)

                if (name.isEmpty()) {
                    dialogBinding.etNamaPengguna.error = "Nama wajib diisi"
                    return@setOnClickListener
                }

                if (email.isEmpty()) {
                    dialogBinding.etEmailPengguna.error = "Email wajib diisi"
                    return@setOnClickListener
                }

                if (!isEdit && password.isEmpty()) {
                    dialogBinding.etPasswordPengguna.error = "Password wajib diisi"
                    return@setOnClickListener
                }

                if (!isEdit && password.length < 6) {
                    dialogBinding.etPasswordPengguna.error = "Password minimal 6 karakter"
                    return@setOnClickListener
                }

                if (isEdit) {
                    val id = pengguna?.id
                    if (id == null) {
                        Toast.makeText(requireContext(), "ID pengguna tidak valid", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    updatePengguna(
                        id = id,
                        name = name,
                        email = email,
                        password = password,
                        role = role,
                        dialog = dialog
                    )
                } else {
                    tambahPengguna(
                        name = name,
                        email = email,
                        password = password,
                        role = role,
                        dialog = dialog
                    )
                }
            }
        }

        dialog.show()
    }

    private fun setSelectedRole(dialogBinding: DialogPenggunaBinding, role: String) {
        val checkedId = when (role.trim().lowercase()) {
            "admin" -> dialogBinding.rbRoleAdmin.id
            "kasir" -> dialogBinding.rbRoleKasir.id
            else -> dialogBinding.rbRoleCustomer.id
        }

        dialogBinding.rgRolePengguna.check(checkedId)
    }

    private fun getSelectedRole(dialogBinding: DialogPenggunaBinding): String {
        return when (dialogBinding.rgRolePengguna.checkedRadioButtonId) {
            dialogBinding.rbRoleAdmin.id -> "admin"
            dialogBinding.rbRoleKasir.id -> "kasir"
            else -> "customer"
        }
    }

    private fun tambahPengguna(
        name: String,
        email: String,
        password: String,
        role: String,
        dialog: AlertDialog
    ) {
        ApiClient.instance.tambahPengguna(name, email, password, role)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadPengguna()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal menambah pengguna",
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

    private fun updatePengguna(
        id: Int,
        name: String,
        email: String,
        password: String,
        role: String,
        dialog: AlertDialog
    ) {
        ApiClient.instance.updatePengguna(id, name, email, password, role)
            .enqueue(object : Callback<ResponseDefault> {
                override fun onResponse(
                    call: Call<ResponseDefault>,
                    response: Response<ResponseDefault>
                ) {
                    val body = response.body()

                    if (response.isSuccessful && body?.status == true) {
                        Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadPengguna()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal mengubah pengguna",
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

    private fun confirmDelete(pengguna: Pengguna) {
        val id = pengguna.id

        if (id == null) {
            Toast.makeText(requireContext(), "ID pengguna tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pengguna")
            .setMessage("Yakin ingin menghapus ${pengguna.name ?: "pengguna ini"}?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                deletePengguna(id)
            }
            .show()
    }

    private fun deletePengguna(id: Int) {
        ApiClient.instance.deletePengguna(id).enqueue(object : Callback<ResponseDefault> {
            override fun onResponse(
                call: Call<ResponseDefault>,
                response: Response<ResponseDefault>
            ) {
                val body = response.body()

                if (response.isSuccessful && body?.status == true) {
                    Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                    loadPengguna()
                } else {
                    Toast.makeText(
                        requireContext(),
                        body?.message ?: "Gagal menghapus pengguna",
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

    private fun updateEmptyState(data: List<Pengguna>) {
        if (data.isEmpty()) {
            b.layoutPenggunaKosong.visibility = View.VISIBLE
            b.rvPengguna.visibility = View.GONE
        } else {
            b.layoutPenggunaKosong.visibility = View.GONE
            b.rvPengguna.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        b.progressPengguna.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
