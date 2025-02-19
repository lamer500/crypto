package com.wallet.app.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wallet.app.databinding.FragmentItemTokenBinding
import com.wallet.app.entity.Token
import com.wallet.app.entity.TokenCombinedData

/**
 * [RecyclerView.Adapter] that can display a [Token].
 */
class TokensAdapter(
    private var values: List<TokenCombinedData>?
) : RecyclerView.Adapter<TokensAdapter.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData(newData: List<TokenCombinedData>?) {
        values = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{

        return ViewHolder(
            FragmentItemTokenBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values?.get(position)
        val tokenInfo = item?.token?.tokenInfo
        holder.tokenName.text = tokenInfo?.name
        holder.tokenBalance.text = "${item?.token?.amount} ${tokenInfo?.symbol}"
        Glide.with(holder.tokenIcon.context).load(tokenInfo?.colorfulImageUrl).into(holder.tokenIcon)
        holder.tokenConvert.text = "$ ${item?.toUSD?:"0"}"
    }

    override fun getItemCount(): Int = values?.size?:0

    inner class ViewHolder(binding: FragmentItemTokenBinding) : RecyclerView.ViewHolder(binding.root) {
        val tokenIcon: ImageView = binding.ivIcon
        val tokenName: TextView = binding.tvName
        val tokenConvert: TextView = binding.tvRate
        val tokenBalance: TextView = binding.tvBalance

        override fun toString(): String {
            return super.toString() + " '" + tokenName.text + "'"
        }
    }

}