package com.klasha.android.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.klasha.android.R
import com.klasha.android.databinding.ActivityWalletLoginBinding

class WalletLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletLoginBinding
    private var isLogin = false

    private var transactionCredentials: TransactionCredentials = TransactionCredentials.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWalletLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            val login = Login(
                "stephen@klasha.com",
                "159Raphael)"
            )
            submit(login)
        }
    }

    private fun submit(login: Login){
        transactionCredentials.login = login
        isLogin = true
        synchronized(transactionCredentials){
            transactionCredentials.notify()
        }
        finish()
    }
}