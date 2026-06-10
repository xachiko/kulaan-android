package com.kulaan.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.databinding.FragmentLoginBinding
import com.kulaan.app.ui.buyer.BuyerMainActivity
import com.kulaan.app.ui.seller.SellerMainActivity
import com.kulaan.app.utils.Resource
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.hide
import com.kulaan.app.utils.show
import com.kulaan.app.utils.toast

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val args: LoginFragmentArgs by navArgs()

    private lateinit var sessionManager: SessionManager
    private val viewModel: AuthViewModel by activityViewModels {
        val session = SessionManager(requireContext())
        val api = ApiClient.getInstance(session)
        AuthViewModelFactory(AuthRepository(api), StoreRepository(api))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sesuaikan title berdasarkan role
        val role = args.role
        binding.tvSubtitle.text = if (role == "seller")
            "Masuk sebagai Penjual" else "Masuk sebagai Pembeli"

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.tvToRegister.setOnClickListener {
            val action = if (role == "seller")
                LoginFragmentDirections.actionLoginToRegisterSeller()
            else
                LoginFragmentDirections.actionLoginToRegisterBuyer()
            findNavController().navigate(action)
        }

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        observeAuth(role)
    }

    private fun observeAuth(role: String) {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.progressBar.show()
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    val data = state.data
                    sessionManager.saveSession(
                        token   = data.token,
                        userId  = data.user.idUser,
                        name    = data.user.name,
                        email   = data.user.email,
                        role    = data.user.role,
                        hasStore = data.user.store != null
                    )
                    // Arahkan ke activity yang sesuai
                    val intent = if (data.user.role == "seller")
                        Intent(requireContext(), SellerMainActivity::class.java)
                    else
                        Intent(requireContext(), BuyerMainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is Resource.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.hide()
                    toast(state.message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
