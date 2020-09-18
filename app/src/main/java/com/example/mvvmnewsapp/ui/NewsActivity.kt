package com.example.mvvmnewsapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mvvmnewsapp.R
import com.example.mvvmnewsapp.db.ArticleDatabase
import com.example.mvvmnewsapp.repository.NewsRepository
import kotlinx.android.synthetic.main.activity_main.*

class NewsActivity : AppCompatActivity() {

    lateinit var viewModel: NewsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository = NewsRepository(ArticleDatabase(this))
        val factory = NewsViewModelProviderFactory(application, repository)
        viewModel = ViewModelProvider(this, factory).get(NewsViewModel::class.java)


        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = "News App"


        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

    }


    fun setBNVVisible(flag: Boolean) {
        bottomNavigationView.visibility = if (flag) View.VISIBLE else View.GONE
    }




}