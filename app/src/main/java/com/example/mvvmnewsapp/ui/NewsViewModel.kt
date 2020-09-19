package com.example.mvvmnewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mvvmnewsapp.NewsApplication
import com.example.mvvmnewsapp.models.ArticlesItem
import com.example.mvvmnewsapp.models.NewsResponse
import com.example.mvvmnewsapp.repository.NewsRepository
import com.example.mvvmnewsapp.util.Resource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class NewsViewModel(
    app: Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(app) {
    val breakingNews: MutableLiveData<Resource<NewsResponse>> =
        MutableLiveData()
    var breakingNewsPage = 1
    private var breakingNewsResponse: NewsResponse? = null

    private var lastSearchQuery = ""
    val searchNews: MutableLiveData<Resource<NewsResponse>> =
        MutableLiveData()
    var searchNewsPage = 1
    private var searchNewsResponse: NewsResponse? = null


    init {
        getBreakingNews("us")
    }


    fun getBreakingNews(countryCode: String) =
        viewModelScope.launch { safeBreakingNewsCall(countryCode) }


    fun getSearchNews(searchQuery: String, from: String, to: String) {
        if (searchQuery != lastSearchQuery) {
            viewModelScope.launch {
                if (searchQuery == "") {
                    safeSearchNewsCall(lastSearchQuery, from, to)
                } else {
                    safeSearchNewsCall(searchQuery, from, to)
                    lastSearchQuery = searchQuery

                }
            }
        }

    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                breakingNewsPage++
                if (breakingNewsResponse == null) breakingNewsResponse = it
                else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)

                }
                return Resource.Success(breakingNewsResponse ?: it)
            }
        }
        return Resource.Error(response.message())
    }

    fun resetResults() {
        searchNewsResponse = null
        searchNewsPage = 1
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                searchNewsPage++
                if (searchNewsResponse == null) searchNewsResponse = it
                else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: it)
            }
        }

        return Resource.Error(response.errorBody().toString())
    }

    fun saveArticle(article: ArticlesItem) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: ArticlesItem) = GlobalScope.launch {
        newsRepository.deleteArticle(article)
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeSearchNewsCall(searchQuery: String, from: String, to: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage, from, to)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else searchNews.postValue(Resource.Error("No internet connection"))
        } catch (t: Throwable) {
            when (t) {
                is SocketTimeoutException -> searchNews.postValue(Resource.Error("Internet speed is low, try again later"))
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))

            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }


        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}