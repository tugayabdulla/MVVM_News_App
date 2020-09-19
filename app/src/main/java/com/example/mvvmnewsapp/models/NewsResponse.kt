package com.example.mvvmnewsapp.models

data class NewsResponse(
	val totalResults: Int,
	val articles: MutableList<ArticlesItem?>,
	val status: String
)




