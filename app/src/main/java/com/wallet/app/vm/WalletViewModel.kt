package com.wallet.app.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.app.entity.TokenCombinedData
import com.wallet.app.utils.JsonHelper
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode


/**
 * WalletViewModel is the ViewModel for the wallet screen.
 */
class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val _combinedData = MutableStateFlow<List<TokenCombinedData>>(emptyList())
    val combinedData: StateFlow<List<TokenCombinedData>> = _combinedData.asStateFlow()

    private val _totalAmount = MutableStateFlow("")
    val totalAmount: StateFlow<String> = _totalAmount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private var sumAmount = BigDecimal.ZERO

    init {
        loadCombinedData()
    }

    @OptIn(FlowPreview::class)
    private fun loadCombinedData() {
        viewModelScope.launch {
            _isLoading.value = true
            JsonHelper.getRates(getApplication<Application>().resources)
                .flatMapConcat { rates ->
                    JsonHelper.getCurrencies(getApplication<Application>().resources
                    ).map { currencies ->
                        Pair(rates, currencies)
                    }
                }
                .flatMapConcat { (rates, currencies) ->
                    JsonHelper.getBalances(getApplication<Application>().resources).map { balances ->
                        Triple(rates, currencies, balances)
                    }
                }
                .map { (rates, currencies, balances) ->
                    sumAmount = BigDecimal.ZERO
                    val tokenCombinedDataList = mutableListOf<TokenCombinedData>()
                    currencies.forEach { currency ->
                        val token = balances.find { balance ->
                            balance.currency == currency.coinId
                        }
                        token?.tokenInfo = currency
                        val rateInfo = rates.find { rate ->
                            rate.fromCurrency == currency.coinId
                        }
                        val rate: String? = rateInfo?.rates?.get(0)?.rate
                        val amount = token?.amount ?: "0"
                        val amountBigDecimal = BigDecimal(amount)
                        val rateBigDecimal = BigDecimal(rate)
                        var convertedAmount = amountBigDecimal.multiply(rateBigDecimal).setScale(8, RoundingMode.DOWN)
                        convertedAmount = convertedAmount.stripTrailingZeros()
                        sumAmount = sumAmount.add(convertedAmount)
                        val toUSD = convertedAmount.toPlainString()

                        if (token != null && rateInfo != null) {
                            val combinedData = TokenCombinedData(
                                token = token,
                                exchangeRate = rateInfo,
                                toUSD = toUSD
                            )
                            tokenCombinedDataList.add(combinedData)
                        }
                    }
                    tokenCombinedDataList
                }.catch { e ->
                    _error.value = "Error loading combined data: ${e.message}"
                }
                .collect { combinedData ->
                    _combinedData.value = combinedData
                    _isLoading.value = false
                    _totalAmount.value = sumAmount.setScale(2, RoundingMode.DOWN).toPlainString()
                }
        }
    }
}