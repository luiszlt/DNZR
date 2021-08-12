package com.example.dnzfind.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.dnzfind.data.Role
import com.example.dnzfind.databinding.RegisterFragmentBinding
import com.google.android.material.chip.Chip

class RegisterFragment : Fragment() {

    private val registerViewModel: RegisterViewModel by activityViewModels()
    private var _binding: RegisterFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = RegisterFragmentBinding.inflate(inflater, container, false)

        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Set up styles to be downloaded directly from the database
        //registerViewModel.styles.asLiveData().observe(viewLifecycleOwner){}

        registerViewModel.toast.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(this.requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        registerViewModel.spinner.observe(viewLifecycleOwner) {
            it?.let { show ->
                binding.loading.visibility = if (show) View.VISIBLE else View.GONE
            }
        }

        binding.register.isEnabled = true
        binding.register.setOnClickListener { registerUser() }

    }

    private fun registerUser() {

        val styles = mutableListOf<String>()
        val userName = binding.username.text.toString()
        val password = binding.password.text.toString()
        var role : String

        //Check username and password
        if (!isUserNameValid()) {
            Toast.makeText(this.requireContext(), "Invalid Username", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordValid()) {
            Toast.makeText(this.requireContext(), "Invalid Password", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isRoleValid()) {
            Toast.makeText(
                this.requireContext(),
                "Please select at least one Role",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else { role = getRole().toString()}

        if (!isStyleValid()) {
            Toast.makeText(
                this.requireContext(),
                "Please select at least one Style",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            binding.stylesChipgroup.checkedChipIds.forEach {
                val chip = binding.stylesChipgroup.findViewById<Chip>(it)
                styles.add(chip.text.toString())
            }
        }

        registerViewModel.createUserEmailPass(userName, password, role, styles, this.requireActivity() )

    }

    private fun isRoleValid(): Boolean {
        return binding.leadChip.isChecked || binding.followChip.isChecked
    }

    private fun isStyleValid(): Boolean {
        return binding.stylesChipgroup.checkedChipIds.size > 0
    }

    private fun isUserNameValid(): Boolean {
        return registerViewModel.isUserNameValid(binding.username.text.toString())
    }

    private fun isPasswordValid(): Boolean {
        return registerViewModel.isPasswordValid(binding.password.text.toString())
    }

    private fun getRole() : Role?{
        return if(binding.leadChip.isChecked && binding.followChip.isChecked) { Role.BOTH }
        else if(binding.leadChip.isChecked) {  Role.LEAD }
        else if(binding.followChip.isChecked) { Role.FOLLOW }
        else return null
    }

}