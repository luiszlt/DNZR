package com.example.dnzfind.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dnzfind.ui.login.LoginFragment
import com.example.dnzfind.ui.register.RegisterFragment

class LoginAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return  2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0   -> RegisterFragment()
            else -> LoginFragment()
        }
    }
}