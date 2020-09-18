package com.example.mvvmnewsapp.api

import com.example.mvvmnewsapp.models.NewsResponse
import com.example.mvvmnewsapp.util.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("/v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country") countryCode: String = "us",
        @Query("page") pageNumber: Int = 1,
        @Query("apiKey") apiKey: String = Constants.API_KEY
    ): Response<NewsResponse>


    @GET("/v2/everything")
    suspend fun searchForNews(
        @Query("q") searchQuery: String,
        @Query("page") pageNumber: Int = 1,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("apiKey") apiKey: String = Constants.API_KEY
    ): Response<NewsResponse>


}