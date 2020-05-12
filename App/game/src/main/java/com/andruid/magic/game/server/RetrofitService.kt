package com.andruid.magic.game.server

import com.andruid.magic.game.model.response.BattleResponse
import com.andruid.magic.game.model.response.RoomResponse
import com.andruid.magic.game.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("/login")
    suspend fun login(@Query("uid") uid: String): Response<UserResponse>

    @GET("/profile")
    suspend fun getProfile(@Query("uid") uid: String): Response<UserResponse>

    @GET("/update-profile")
    suspend fun  updateProfile(@Query("uid") uid: String, @Query("user_name") userName: String,
                              @Query("avatar_uri") avatarUri: String): Response<UserResponse>

    @GET("/create-battle")
    suspend fun createBattle(@Query("uid") uid: String, @Query("coins") coins: Int): Response<BattleResponse>

    @GET("/join-battle")
    suspend fun joinBattle(@Query("uid") uid: String, @Query("bid") bid: String): Response<BattleResponse>

    @GET("/my-rooms")
    suspend fun getMyRooms(@Query("uid") uid: String, @Query("page_start") pageStart: Int,
                           @Query("page_size") pageSize: Int): Response<RoomResponse>
}