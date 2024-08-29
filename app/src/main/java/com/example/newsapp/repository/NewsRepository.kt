package com.example.newsapp.repository

import com.example.newsapp.Result
import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.db.ResultDatabase

class NewsRepository(val db: ResultDatabase) {
    suspend fun getHeadlines(countryCode: String) =
        RetrofitInstance.api.getHeadlines(countryCode)
    suspend fun searchNews(searchQuery: String)=
        RetrofitInstance.api.searchForNews(searchQuery)
    suspend fun upsert(result: Result) = db.getResultDao().upsert(result)
    fun getFavouriteNews() = db.getResultDao().getAllResults()
    suspend fun deleteResult(result: Result) = db.getResultDao().deleteResult(result)
}