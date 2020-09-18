package com.example.mvvmnewsapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mvvmnewsapp.models.ArticlesItem

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArticle(article: ArticlesItem): Long


    @Query("SELECT * FROM articles")
    fun getArticles(): LiveData<List<ArticlesItem>>

    @Delete
    suspend fun deleteArticle(article: ArticlesItem)

}