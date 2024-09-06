package com.example.newsapp.repository

import android.util.Log
import com.example.newsapp.NewsResponse
import com.example.newsapp.Result
import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.db.ResultDatabase
import retrofit2.Response

class NewsRepository(val db: ResultDatabase) {
    suspend fun getHeadlines(countryCode: String): Response<NewsResponse> {
        return RetrofitInstance.api.getHeadlinesInit()
    }

    suspend fun getNextPage(nextPageUrl: String): Response<NewsResponse> {
        return RetrofitInstance.api.getHeadlines(page=nextPageUrl)
    }
    suspend fun searchNews(searchQuery: String)=
        RetrofitInstance.api.searchForNews(searchQuery)
    suspend fun upsert(result: Result) = db.getResultDao().upsert(result)
    fun getFavouriteNews() = db.getResultDao().getAllResults()
    suspend fun deleteResult(result: Result) = db.getResultDao().deleteResult(result)
}