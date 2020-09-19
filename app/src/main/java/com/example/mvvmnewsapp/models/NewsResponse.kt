package com.example.mvvmnewsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

data class NewsResponse(
	val totalResults: Int,
	val articles: MutableList<ArticlesItem?>,
	val status: String
)




