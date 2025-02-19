package com.wallet.app.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.wallet.app.R
import com.wallet.app.adapter.TokensAdapter
import com.wallet.app.databinding.FragmentItemListBinding
import com.wallet.app.vm.WalletViewModel
import kotlinx.coroutines.launch

/**
 * Supported wallet list screen.
 */
class WalletFragment : Fragment() {

    private val viewModel: WalletViewModel by viewModels()
    private var adapter:TokensAdapter?=null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentItemListBinding.inflate(
            LayoutInflater.from(context),
            container,
            false
        )
        adapter = TokensAdapter(null)

        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@WalletFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalAmount.collect { totalAmount ->
                    binding.tvAmount.text = setAmountColor("$ $totalAmount USD")
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.combinedData.collect { combinedDataList ->
                    adapter?.refreshData(combinedDataList)
                }
            }
        }
        wrapStatusBar()
        return binding.root
    }

    private fun wrapStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.apply {
                setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS.inv(),
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else {
            @Suppress("DEPRECATION")
            requireActivity().window.decorView.systemUiVisibility =
                requireActivity().window.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun setAmountColor(amount: String):CharSequence{
        return buildSpannedString {
            val length = amount.length
            val grayColor = resources.getColor(R.color.color_gray, null)
            if (length > 4){
                append(android.text.SpannableString(amount.substring(0,1)).apply {  setSpan(android.text.style.ForegroundColorSpan(grayColor),0,1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
                append(android.text.SpannableString(amount.substring(1,length-3)).apply {  setSpan(android.text.style.ForegroundColorSpan(Color.WHITE),0,length-4, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
                append(android.text.SpannableString(amount.substring(length-3)).apply {  setSpan(android.text.style.ForegroundColorSpan(grayColor),0,3, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })

            }
        }
    }
}