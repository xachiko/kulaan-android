package com.kulaan.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kulaan.app.R
import com.kulaan.app.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol Pembeli → LoginFragment dengan role=buyer
        binding.btnBuyer.setOnClickListener {
            val action = WelcomeFragmentDirections
                .actionWelcomeToLogin(role = "buyer")
            findNavController().navigate(action)
        }

        // Tombol Penjual → LoginFragment dengan role=seller
        binding.btnSeller.setOnClickListener {
            val action = WelcomeFragmentDirections
                .actionWelcomeToLogin(role = "seller")
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
