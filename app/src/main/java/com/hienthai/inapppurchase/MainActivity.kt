package com.hienthai.inapppurchase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.hienthai.inapppurchase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setAction()
    }



    private fun setAction() {
        binding.cardPurchase.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }

        binding.cardSubscription.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }
    }

}