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

        // Gọi đến phương thức newBuilder() để tạo ra một instance của BillingClient, tiếp đến bạn cũng phải gọi đến phương thức setListener để lắng nghe được sự kiện của PurchasesUpdatedListener để nhận được những cập nhật purchases bởi ứng dụng của bạn.
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        // Thiết lập một kết nối đối với Google Play. Sử dụng phương thức startConnection() để bắt đầu kết nối và dữ liệu sẽ nhận về thông qua BillingClientStateListener.
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

            // Override phương thức onBillingServiceDisconnected() và xử lý khi bị mất liên kết với Google Play. Ví dụ như kết nối có thể bị mất khi Google Play Store Service cập nhật bên trong background. Chính vì vậy nên cần phải gọi startConnection() để tạo lại một connection trước khi thực hiện lại 1 yêu cầu.
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
        // tạo list các product id (chính là product id bạn đã nhập ở bước trước) để lấy thông tin
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
        // Hàm này sẽ trả về kết quả khi người dùng thực hiện mua hàng.
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