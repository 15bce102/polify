package com.andruid.magic.game.server

import com.andruid.magic.game.model.response.BattleResponse
import com.andruid.magic.game.model.response.QuestionsResponse
import com.andruid.magic.game.model.response.RoomResponse
import com.andruid.magic.game.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("/login")
    suspend fun login(@Query("uid") uid: String): Response<UserResponse>

    @GET("/fetch-profile")
    suspend fun getProfile(@Query("uid") uid: String): Response<UserResponse>

    @GET("/update-profile")
    suspend fun updateProfile(@Query("uid") uid: String, @Query("user_name") userName: String,
                              @Query("avatar_uri") avatarUri: String): Response<UserResponse>

    @GET("/update-token")
    suspend fun updateToken(@Query("uid") uid: String, @Query("token") token: String): Response<UserResponse>

    @GET("/join-waiting-room")
    suspend fun joinWaitingRoom(@Query("uid") uid: String): Response<BattleResponse>

    @GET("/leave-waiting-room")
    suspend fun leaveWaitingRoom(@Query("uid") uid: String): Response<BattleResponse>

    @GET("/get-questions")
    suspend fun getQuestions(@Query("bid") bid: String): Response<QuestionsResponse>

    @GET("/create-battle")
    suspend fun createBattle(@Query("uid") uid: String, @Query("coins") coins: Int): Response<BattleResponse>

    @GET("/join-battle")
    suspend fun joinBattle(@Query("uid") uid: String, @Query("bid") bid: String): Response<BattleResponse>

    @GET("/my-rooms")
    suspend fun getMyRooms(@Query("uid") uid: String, @Query("page_start") pageStart: Int,
                           @Query("page_size") pageSize: Int): Response<RoomResponse>
}