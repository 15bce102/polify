package com.droidx.gameapi.server

import com.droidx.gameapi.model.response.*
import retrofit2.Response
import retrofit2.http.*

interface RetrofitService {

    /** User related requests **/

    @Headers("Content-Type: application/json")
    @POST("/check-user-exists")
    suspend fun checkIfUserExists(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/signup")
    suspend fun signup(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/login")
    suspend fun login(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/update-token")
    suspend fun updateToken(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/update-status")
    suspend fun updateStatus(@Query("status") status: Int,
                             @Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/update-avatar")
    suspend fun updateAvatar(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/fetch-profile")
    suspend fun getProfile(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<UserResponse>

    @Headers("Content-Type: application/json")
    @POST("/update-friends")
    suspend fun updateFriends(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<UserResponse>

    @GET("/get-avatars")
    suspend fun getAvatars(): Response<AvatarResponse>

    @Headers("Content-Type: application/json")
    @POST("/my-friends")
    suspend fun getMyFriends(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<FriendsResponse>

    @Headers("Content-Type: application/json")
    @POST("/add-coins")
    suspend fun addCoins(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    /** Battle related requests **/

    @Headers("Content-Type: application/json")
    @POST("/create-room")
    suspend fun createMultiPlayerRoom(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<RoomResponse>

    @Headers("Content-Type: application/json")
    @POST("/leave-room")
    suspend fun leaveMultiPlayerRoom(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/send-invite")
    suspend fun sendMultiPlayerInvite(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/join-room")
    suspend fun joinRoom(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<RoomResponse>

    @Headers("Content-Type: application/json")
    @POST("/start-battle")
    suspend fun startMultiPlayerBattle(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/leave-battle")
    suspend fun leaveBattle(@Body map: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse>

    @GET("/join-waiting-room")
    suspend fun joinWaitingRoom(@Query("uid") uid: String): Response<BattleResponse>

    @GET("/leave-waiting-room")
    suspend fun leaveWaitingRoom(@Query("uid") uid: String): Response<BattleResponse>

    @GET("/get-questions")
    suspend fun getQuestions(@Query("bid") bid: String): Response<QuestionsResponse>

    @GET("/update-score")
    suspend fun updateScore(@Query("bid") bid: String, @Query("uid") uid: String,
                            @Query("score") score: Int): Response<BattleResponse>
}