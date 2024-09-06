package com.example.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.NewsResponse
import com.example.newsapp.Result
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import okio.IOException

class NewsViewModel(app: Application, val newsRepository: NewsRepository): AndroidViewModel(app) {
    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null
    var nextPageUrl: String? = null
    private var isLoading = false

    // Fetch headlines
    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        if (isLoading) return@launch

        isLoading = true
        headlines.postValue(Resource.Loading())

        try {
            val response = if (nextPageUrl == null) {
                newsRepository.getHeadlines(countryCode)
            } else {
                newsRepository.getNextPage(nextPageUrl!!)
            }
            headlines.postValue(handleHeadlinesResponse(response))
        } catch (e: IOException) {
            headlines.postValue(Resource.Error("Unable to connect"))
        } finally {
            isLoading = false
        }
    }

    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                nextPageUrl = resultResponse.nextPage

                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    init {
        getHeadlines("vi")
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse
                } else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.results
                    val newArticles = resultResponse.results
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToFavourites(article: Result) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteArticle(article: Result) = viewModelScope.launch {
        newsRepository.deleteResult(article)
    }

    fun internetConnection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }

    private suspend fun headlinesInternet(countryCode: String) {
        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())) {
                val response = newsRepository.getHeadlines(countryCode)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery: String) {
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())) {
                val response = newsRepository.searchNews(searchQuery)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Unable to connect"))
                else -> searchNews.postValue(Resource.Error("No signal"))
            }
        }
    }
}
