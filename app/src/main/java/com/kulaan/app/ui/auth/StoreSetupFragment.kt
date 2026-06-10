package com.kulaan.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.databinding.FragmentStoreSetupBinding
import com.kulaan.app.ui.seller.SellerMainActivity
import com.kulaan.app.utils.Resource
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.hide
import com.kulaan.app.utils.show
import com.kulaan.app.utils.toast

class StoreSetupFragment : Fragment() {

    private var _binding: FragmentStoreSetupBinding? = null
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
        _binding = FragmentStoreSetupBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSetup.setOnClickListener {
            viewModel.setupStore(
                storeName      = binding.etStoreName.text.toString().trim(),
                description    = binding.etDescription.text.toString().trim().ifBlank { null },
                address        = binding.etAddress.text.toString().trim(),
                district       = binding.etDistrict.text.toString().trim(),
                operatingHours = binding.etOperatingHours.text.toString().trim().ifBlank { null }
            )
        }

        viewModel.storeState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.btnSetup.isEnabled = false
                    binding.progressBar.show()
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    sessionManager.setHasStore(true)
                    toast("Toko berhasil dibuat! Menunggu verifikasi admin.")
                    startActivity(Intent(requireContext(), SellerMainActivity::class.java))
                    requireActivity().finish()
                }
                is Resource.Error -> {
                    binding.btnSetup.isEnabled = true
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
