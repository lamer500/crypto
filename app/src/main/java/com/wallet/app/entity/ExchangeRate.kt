package com.wallet.app.entity
import com.google.gson.annotations.SerializedName

data class ExchangeRate(
    @SerializedName("from_currency")
    val fromCurrency: String,
    @SerializedName("to_currency")
    val toCurrency: String,
    @SerializedName("rates")
    val rates: List<Rate>,
    @SerializedName("time_stamp")
    val timeStamp: Long
)

data class Rate(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("rate")
    val rate: String
)