package com.wallet.app.entity

data class TokenCombinedData(
    val token: Token,
    val exchangeRate: ExchangeRate,
    var toUSD:String
)
