package com.example.mvvmnewsapp.repository

import com.example.mvvmnewsapp.api.RetrofitInstance
import com.example.mvvmnewsapp.db.ArticleDatabase
import com.example.mvvmnewsapp.models.ArticlesItem

class NewsRepository(private val db: ArticleDatabase) {


    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(
        searchQuery: String,
        pageNumber: Int,
        from: String = "",
        to: String = ""
    ) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber, from, to)


    suspend fun upsert(article: ArticlesItem) = db.getArticleDao().upsertArticle(article)
    suspend fun deleteArticle(article: ArticlesItem) = db.getArticleDao().deleteArticle(article)
    fun getSavedNews() = db.getArticleDao().getArticles()
}