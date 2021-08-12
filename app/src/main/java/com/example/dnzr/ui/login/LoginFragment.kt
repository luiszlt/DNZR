package com.example.dnzfind.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.dnzfind.databinding.LoginFragmentBinding
import com.example.dnzfind.ui.register.RegisterViewModel

class LoginFragment : Fragment() {

    private var _binding: LoginFragmentBinding? = null
    private val loginViewModel: RegisterViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = LoginFragmentBinding.inflate(inflater, container, false)

        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.toast.observe(viewLifecycleOwner){
            it?.let {
                Toast.makeText(this.requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        loginViewModel.spinner.observe(viewLifecycleOwner) {
            it?.let { show ->
                binding.loading.visibility = if (show) View.VISIBLE else View.GONE
            }
        }

        binding.login.isEnabled = true
        binding.login.setOnClickListener { loginWithEmailPasswordCombo() }
    }

    private fun loginWithEmailPasswordCombo(){

        val userName = binding.username.text.toString()
        val password = binding.password.text.toString()

        if(loginViewModel.isPasswordValid(userName) && loginViewModel.isPasswordValid(password)){
            loginViewModel.loginUserEmailPass(userName, password, this.requireActivity())
        }

    }

}