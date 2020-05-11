package com.andruid.magic.game.util

import com.andruid.magic.game.model.response.ApiResponse
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException

suspend fun <T : ApiResponse> sendNetworkRequest(requestFunc: suspend () -> Response<T>): Response<T>? {
    return try {
        requestFunc.invoke()
    } catch (e: HttpException) {
        null
    } catch (e: ConnectException) {
        null
    } catch (e: IOException) {
        null
    }
}