package com.example.newsapp.api

import com.example.newsapp.NewsResponse
import com.example.newsapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {
    @GET("api/1/news")
    suspend fun getHeadlines (
        @Query("language")
        countryCode: String = "vi",
        @Query("apikey")
        apiKey: String = API_KEY
    ):Response<NewsResponse>
    @GET("api/1/news")
    suspend fun searchForNews (
        @Query("q")
        searchQuery: String,
        @Query("language")
        countryCode: String = "vi",
        @Query("apikey")
        apiKey: String = API_KEY
    ):Response<NewsResponse>
}