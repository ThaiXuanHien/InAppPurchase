package com.hienthai.inapppurchase

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.hienthai.inapppurchase.databinding.ActivityPurchaseBinding

class PurchaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPurchaseBinding
    private lateinit var billingClient: BillingClient
    private lateinit var oneTimeProductAdapter: OneTimeProductAdapter
    private lateinit var consumeResponseListener: ConsumeResponseListener
    private var skuListDetail: MutableList<SkuDetails> = emptyList<SkuDetails>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBilling()
        setupRecyclerView()
        binding.btnLoadProduct.setOnClickListener {
            queryProduct()
        }
    }

    private fun setupRecyclerView() {
        oneTimeProductAdapter = OneTimeProductAdapter(skuListDetail, ::onClick)
        binding.rcvItemOneTime.layoutManager = LinearLayoutManager(this)
        binding.rcvItemOneTime.setHasFixedSize(true)
        binding.rcvItemOneTime.adapter = oneTimeProductAdapter

    }

    private fun onClick(position: Int) {
        val billingFlowParam = BillingFlowParams
            .newBuilder()
            .setSkuDetails(skuListDetail[position])
            .build()
        billingClient.launchBillingFlow(this, billingFlowParam)
    }

    private fun initBilling() {

        consumeResponseListener = ConsumeResponseListener { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Toast.makeText(
                    this@PurchaseActivity,
                    "Consume Success",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // G???i ?????n ph????ng th???c newBuilder() ????? t???o ra m???t instance c???a BillingClient, ti???p ?????n b???n c??ng ph???i g???i ?????n ph????ng th???c setListener ????? l???ng nghe ???????c s??? ki???n c???a PurchasesUpdatedListener ????? nh???n ???????c nh???ng c???p nh???t purchases b???i ???ng d???ng c???a b???n.
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        // Thi???t l???p m???t k???t n???i ?????i v???i Google Play. S??? d???ng ph????ng th???c startConnection() ????? b???t ?????u k???t n???i v?? d??? li???u s??? nh???n v??? th??ng qua BillingClientStateListener.
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Toast.makeText(
                        this@PurchaseActivity,
                        "Connect Billing Success",
                        Toast.LENGTH_SHORT
                    ).show()
                    val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList
                    purchases?.let { handleItemAlreadyPurchase(it) }
                } else {
                    Toast.makeText(
                        this@PurchaseActivity,
                        "Error code: ${billingResult.responseCode}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Override ph????ng th???c onBillingServiceDisconnected() v?? x??? l?? khi b??? m???t li??n k???t v???i Google Play. V?? d??? nh?? k???t n???i c?? th??? b??? m???t khi Google Play Store Service c???p nh???t b??n trong background. Ch??nh v?? v???y n??n c???n ph???i g???i startConnection() ????? t???o l???i m???t connection tr?????c khi th???c hi???n l???i 1 y??u c???u.
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Toast.makeText(
                    this@PurchaseActivity,
                    "You are disconnect from Billing Service",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun handleItemAlreadyPurchase(purchases: List<Purchase>) {
        var strPremium = ""
        for (purchase in purchases){
            if(purchase.skus.equals("reward_product")){
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.consumeAsync(consumeParams, consumeResponseListener)
            }
            strPremium += "\n${purchase.skus}"
        }
        binding.txtPremium.text = strPremium
        binding.txtPremium.visibility = View.VISIBLE
    }

    private fun queryProduct() {
        // t???o list c??c product id (ch??nh l?? product id b???n ???? nh???p ??? b?????c tr?????c) ????? l???y th??ng tin
        val skuList = listOf("one_time_product", "reward_product")
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP) //  .setType(BillingClient.SkuType.SUBS)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d(TAG, "onSkuDetailResponse() success, list: $skuDetailsList")
                    skuDetailsList?.let {
                        if (it.isNotEmpty()) {
                            skuListDetail.addAll(it)
                        }
                    }
                }
                else -> {
                    Log.d(
                        TAG,
                        "onSkuDetailResponse() fail, responseCode: ${billingResult.responseCode}"
                    )
                }
            }
        }
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        // H??m n??y s??? tr??? v??? k???t qu??? khi ng?????i d??ng th???c hi???n mua h??ng.
        when(billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let {
                    // Handle response success
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // Handle response cancel
            }
        }

    }

    companion object {
        private const val TAG = "PurchaseActivity"
    }

}