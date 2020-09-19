package com.example.mvvmnewsapp.util

import java.text.SimpleDateFormat
import java.util.*

object Helper {

    private val sdfIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val date = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    private lateinit var dateObject: Date
    fun setDate(text: String) {

        dateObject = sdfIn.parse(text.substring(0, text.lastIndex - 1))!!
    }

    fun getDate(): String {
        return date.format(dateObject)
    }

    fun getTime(): String {
        return time.format(dateObject)
    }

}