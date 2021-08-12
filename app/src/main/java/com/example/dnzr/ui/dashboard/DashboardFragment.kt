package com.example.dnzfind.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.dnzfind.data.Role
import com.example.dnzfind.databinding.FragmentDashboardBinding
import com.example.dnzfind.ui.home.HomeViewModel
import com.google.android.material.chip.Chip

class DashboardFragment : Fragment() {

    private val dashboardViewModel: HomeViewModel by activityViewModels()
    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        const val TAG = "Dashboard Fragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userNameLabel.text = dashboardViewModel.userName.toString()

        dashboardViewModel.dnzr.observe(this.viewLifecycleOwner){
            setStyleChips(it.styles)
            setRoleChips(it.role)
            setStatus(it.status)
        }

        binding.saveAction.setOnClickListener { updateUser() }
    }

    private fun setStyleChips(styles : List<String>){

        if (styles != null) {
            for (style in styles) {
                val styleStr = "${style.toString()}_chip"
                val styleChip = this.requireContext().resources.getIdentifier(styleStr, "id", this.requireContext().packageName)

                binding.stylesChipgroup.check(styleChip)
            }
        }
    }

    private fun setRoleChips(role : String){

        when (role){
            Role.FOLLOW.toString() -> { binding.followChip.isChecked = true}
            Role.LEAD.toString() -> { binding.leadChip.isChecked = true }
            Role.BOTH.toString() -> { binding.leadChip.isChecked = true
                                        binding.followChip.isChecked = true }
            else -> binding.rolesChipgroup.clearCheck()
        }
    }

    private fun setStatus(status : String){
        binding.statusDesc.setText (status)
    }

    private fun updateUser() {
        val styles = mutableListOf<String>()
        val status = binding.statusDesc.text.toString()

        if(isStyleValid()){
            binding.stylesChipgroup.checkedChipIds.forEach {
                val chip = binding.stylesChipgroup.findViewById<Chip>(it)
                styles.add(chip.text.toString())
            }
            dashboardViewModel.updateUserStyles(styles)
        }

        if(isRoleValid()) {
            dashboardViewModel.updateUserRole(getRole()?.toString() ?: "")
        }

        if(status != "" && status.length <= 180){
            dashboardViewModel.updateUserStatus(status)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getRole() : Role?{
        if(binding.leadChip.isChecked && binding.followChip.isChecked) { return Role.BOTH }
        else if(binding.leadChip.isChecked) { return Role.LEAD }
        else if(binding.followChip.isChecked) {return Role.FOLLOW }
        else return null
    }

    private fun isRoleValid(): Boolean {
        Log.d(TAG, "Lead: ${binding.leadChip.isChecked} Follow: ${binding.followChip.isChecked}")
        return binding.leadChip.isChecked || binding.followChip.isChecked
    }

    private fun isStyleValid(): Boolean {
        return binding.stylesChipgroup.checkedChipIds.size > 0
    }
}