package com.andruid.magic.game.util

import com.andruid.magic.game.model.response.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException

suspend fun <T : ApiResponse> sendNetworkRequest(requestFunc: suspend () -> Response<T>): Response<T>? {
    return withContext(Dispatchers.IO) {
        try {
            requestFunc.invoke()
        } catch (e: HttpException) {
            null
        } catch (e: ConnectException) {
            null
        } catch (e: IOException) {
            null
        }
    }
}