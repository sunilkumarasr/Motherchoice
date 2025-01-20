package com.royalit.motherchoice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.royalit.motherchoice.databinding.FirstScreenBinding

class First_Screen : Activity() {
    private lateinit var binding: FirstScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FirstScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signinText.setOnClickListener {

            val intent = Intent(
                this,
                SignIn_Screen::class.java
            )
            startActivity(intent)
            finish()
        }
        binding.signupText.setOnClickListener {


            val intent = Intent(
                this,
                Signup_Screen::class.java
            )
            startActivity(intent)
            finish()
        }

    }
}