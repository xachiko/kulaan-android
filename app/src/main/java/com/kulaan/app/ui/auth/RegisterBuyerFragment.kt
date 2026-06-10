package com.kulaan.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.databinding.FragmentRegisterBuyerBinding
import com.kulaan.app.ui.buyer.BuyerMainActivity
import com.kulaan.app.utils.Resource
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.hide
import com.kulaan.app.utils.show
import com.kulaan.app.utils.toast

class RegisterBuyerFragment : Fragment() {

    private var _binding: FragmentRegisterBuyerBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val viewModel: AuthViewModel by activityViewModels {
        val session = SessionManager(requireContext())
        val api = ApiClient.getInstance(session)
        AuthViewModelFactory(AuthRepository(api), StoreRepository(api))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBuyerBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            viewModel.registerBuyer(
                name            = binding.etName.text.toString().trim(),
                email           = binding.etEmail.text.toString().trim(),
                password        = binding.etPassword.text.toString().trim(),
                confirmPassword = binding.etConfirmPassword.text.toString().trim()
            )
        }

        binding.tvToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.progressBar.show()
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    val data = state.data
                    sessionManager.saveSession(
                        token    = data.token,
                        userId   = data.user.idUser,
                        name     = data.user.name,
                        email    = data.user.email,
                        role     = data.user.role,
                        hasStore = false
                    )
                    startActivity(Intent(requireContext(), BuyerMainActivity::class.java))
                    requireActivity().finish()
                }
                is Resource.Error -> {
                    binding.btnRegister.isEnabled = true
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
