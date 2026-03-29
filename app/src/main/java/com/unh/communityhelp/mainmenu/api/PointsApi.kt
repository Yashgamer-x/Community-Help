package com.unh.communityhelp.mainmenu.api

import com.google.firebase.Timestamp
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class PointsRequest(
    val title: String,
    val description: String,
    val timestamp: Timestamp,
    val rating: Int,
    val comment: String,
    val location: String,
)
data class PointsResponse(val points: Long)

interface PointsApi {
    @POST("predict")
    suspend fun calculatePoints(@Body request: PointsRequest): Response<PointsResponse>
}