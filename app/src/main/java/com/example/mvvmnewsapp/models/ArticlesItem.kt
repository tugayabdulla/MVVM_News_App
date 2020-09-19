package com.example.mvvmnewsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "articles"
)
data class ArticlesItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val publishedAt: String?=null,
    val author: String?=null,
    val urlToImage: String?=null,
    val description: String?=null,
    val source: Source?=null,
    val title: String?=null,
    val url: String?=null,
    val content: String? = null
): Serializable