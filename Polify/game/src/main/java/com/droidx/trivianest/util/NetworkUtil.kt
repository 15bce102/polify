package com.droidx.trivianest.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import com.droidx.trivianest.model.response.Result

suspend fun <T> sendNetworkRequest(requestFunc: suspend () -> Response<T>): Result<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = requestFunc.invoke()
            Result.success<T>(response.body())
        } catch (e: HttpException) {
            Result.error<T>(e.message())
        } catch (e: ConnectException) {
            Result.error<T>(e.message ?: "ConnectException")
        } catch (e: IOException) {
            Result.error<T>(e.message ?: "IOException")
        }
    }
}