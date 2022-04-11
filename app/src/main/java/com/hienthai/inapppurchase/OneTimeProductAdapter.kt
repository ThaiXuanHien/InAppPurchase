package com.hienthai.inapppurchase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.hienthai.inapppurchase.databinding.ItemOneTimeProductBinding

class OneTimeProductAdapter(
    private val skuList: MutableList<SkuDetails>,
    private val onClick: (position: Int) -> Unit
) :
    RecyclerView.Adapter<OneTimeProductAdapter.OneTimeProductViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OneTimeProductViewHolder {
        val binding = ItemOneTimeProductBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return OneTimeProductViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: OneTimeProductViewHolder, position: Int) {
        val skuDetail = skuList[position]
        holder.bind(skuDetail)
    }

    override fun getItemCount() = skuList.size

    class OneTimeProductViewHolder(
        val binding: ItemOneTimeProductBinding,
        val onClick: (postion: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(skuDetail: SkuDetails) {
            binding.txtTitle.text = skuDetail.title
            binding.txtPrice.text = skuDetail.price
            binding.txtDescription.text = skuDetail.description

            binding.layoutItem.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }

}