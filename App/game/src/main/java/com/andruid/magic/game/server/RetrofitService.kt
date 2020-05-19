package com.andruid.magic.game.server

import com.andruid.magic.game.model.response.*
import retrofit2.Response
import retrofit2.http.*

interface RetrofitService {
    @GET("/check-user-exists")
    suspend fun checkIfUserExists(@Query("phoneNumber") phoneNumber: String): Response<UserResponse>

    @GET("/login")
    suspend fun login(@Query("uid") uid: String): Response<UserResponse>

    @GET("/fetch-profile")
    suspend fun getProfile(@Query("uid") uid: String): Response<UserResponse>

    @GET("/update-profile")
    suspend fun updateProfile(@Query("uid") uid: String, @Query("user_name") userName: String,
                              @Query("avatar") avatar: String): Response<UserResponse>

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

    @Headers("Content-Type: application/json")
    @POST("/update-friends")
    suspend fun updateFriends(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<UserResponse>

    @GET("/create-room")
    suspend fun createMultiPlayerRoom(@Query("uid") uid: String): Response<RoomResponse>

    @GET("/my-friends")
    suspend fun getMyFriends(@Query("uid") uid: String): Response<FriendsResponse>

    @GET("/create-battle")
    suspend fun createBattle(@Query("uid") uid: String, @Query("coins") coins: Int): Response<BattleResponse>

    @GET("/join-battle")
    suspend fun joinBattle(@Query("uid") uid: String, @Query("bid") bid: String): Response<BattleResponse>
}