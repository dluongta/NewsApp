package com.example.newsapp

data class NewsResponse(
    val nextPage: String,
    val results: MutableList<Result>,
    val status: String,
    val totalResults: Int
)