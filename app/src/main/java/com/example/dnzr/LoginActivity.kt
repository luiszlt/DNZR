package com.example.dnzfind

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dnzfind.adapters.LoginAdapter
import com.example.dnzfind.databinding.ActivityLoginBinding
import com.example.dnzfind.ui.register.RegisterViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var registerviewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerviewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        if(registerviewModel.validUser){ registerviewModel.startMainActivitiy(this) }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val pageAdapter = LoginAdapter(this)
        viewPager.adapter = pageAdapter

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.text = when(position){
                0 -> "Register"
                else -> "Login"
            }
        }.attach()

        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
    }
}