package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.databinding.LoginFragmentBinding

class LoginFragment : Fragment() {

    private lateinit var b: LoginFragmentBinding
    private lateinit var db: DBOpenHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = LoginFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBOpenHelper(requireContext())

        b.btnLogin.setOnClickListener {
            val email = b.etEmail.text.toString()
            val pass = b.etPassword.text.toString()

            val role = db.checkLogin(email, pass)

            if (role != null) {
                Toast.makeText(requireContext(), "Login sebagai $role", Toast.LENGTH_SHORT).show()

                if (role == "admin") {
                    Toast.makeText(requireContext(), "Masuk Admin Page", Toast.LENGTH_SHORT).show()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                Toast.makeText(requireContext(), "Login gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}