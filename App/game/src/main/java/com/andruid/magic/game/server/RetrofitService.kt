package com.andruid.magic.game.server

import com.andruid.magic.game.model.response.*
import retrofit2.Response
import retrofit2.http.Body
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

    @GET("/update-score")
    suspend fun updateScore(@Query("bid") bid: String, @Query("uid") uid: String,
                            @Query("score") score: Int): Response<BattleResponse>

    @GET("/get-avatars")
    suspend fun getAvatars(): Response<AvatarResponse>

    @GET("/update-status")
    suspend fun updateStatus(@Query("uid") uid: String,
                             @Query("status") status: Int): Response<UserResponse>

    @GET("/update-friends")
    suspend fun updateFriends(@Body map: Map<String, Any>): Response<UserResponse>

    @GET("/create-battle")
    suspend fun createBattle(@Query("uid") uid: String, @Query("coins") coins: Int): Response<BattleResponse>

    @GET("/join-battle")
    suspend fun joinBattle(@Query("uid") uid: String, @Query("bid") bid: String): Response<BattleResponse>

    @GET("/my-rooms")
    suspend fun getMyRooms(@Query("uid") uid: String, @Query("page_start") pageStart: Int,
                           @Query("page_size") pageSize: Int): Response<RoomResponse>
}