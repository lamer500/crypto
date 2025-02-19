package com.wallet.app.utils

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wallet.app.R
import com.wallet.app.entity.ExchangeRate
import com.wallet.app.entity.Token
import com.wallet.app.entity.TokenInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Helper object for reading and parsing JSON data from raw resources.
 */
object JsonHelper {
    private fun <T> getObjectsFromRaw(resources: Resources, resourceId: Int, clazz: Class<T>): Flow<List<T>> = flow {
        val jsonString = withContext(Dispatchers.IO) {
            var inputStream: java.io.InputStream? = null
            try {
                inputStream = resources.openRawResource(resourceId)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                stringBuilder.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext null
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        if (jsonString == null) {
            throw Exception("Failed to read raw file: $resourceId")
        }
        val products = try {
            val gson = Gson()
            val listType = TypeToken.getParameterized(List::class.java, clazz).type
            val result: List<T> = gson.fromJson(jsonString, listType)

            result

        } catch (e: Exception) {
            throw Exception("Error parsing JSON from raw file: $resourceId", e)
        }
        emit(products)
    }.catch { e ->
        println("Error in getProductsFromRaw: ${e.message}")
        emit(emptyList())
    }

    fun getRates(resources: Resources): Flow<List<ExchangeRate>> = getObjectsFromRaw(resources, R.raw.rates, ExchangeRate::class.java)

    fun getCurrencies(resources: Resources): Flow<List<TokenInfo>> = getObjectsFromRaw(resources, R.raw.currencies, TokenInfo::class.java)

    fun getBalances(resources: Resources): Flow<List<Token>> = getObjectsFromRaw(resources, R.raw.balance, Token::class.java)
}