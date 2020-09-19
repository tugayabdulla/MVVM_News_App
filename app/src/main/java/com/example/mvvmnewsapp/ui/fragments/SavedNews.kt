package com.example.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmnewsapp.R
import com.example.mvvmnewsapp.adapters.NewsAdapter
import com.example.mvvmnewsapp.models.ArticlesItem
import com.example.mvvmnewsapp.ui.NewsActivity
import com.example.mvvmnewsapp.ui.NewsViewModel
import com.example.mvvmnewsapp.util.TouchHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_saved_news.*


class SavedNews : Fragment(R.layout.fragment_saved_news), TouchHelper.OnSwipeListener {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = (activity as NewsActivity).viewModel
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()



        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_savedNews_to_article, bundle)
        }

        viewModel.getSavedNews().observe(viewLifecycleOwner, {
            newsAdapter.differ.submitList(it)
        })

        val itemTouchHelper = TouchHelper(this)
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(rvSavedNews)

    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()

        rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onResume() {
        (activity as NewsActivity).apply {
            setBNVVisible(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
        super.onResume()
    }

    private fun showSnack(view: View, article: ArticlesItem) {
        val snack =
            Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG)
                .apply {
                    setAction("Undo") {
                        viewModel.saveArticle(article)
                    }
                }
        val params = snack.view.layoutParams as CoordinatorLayout.LayoutParams
        params.setMargins(
            0,
            0,
            0,
            (activity as NewsActivity).bottomNavigationView.height
        )
        snack.view.layoutParams = params
        snack.show()
    }

    override fun swiped(position: Int) {
        val article = newsAdapter.differ.currentList[position]
        viewModel.deleteArticle(article)
        showSnack(requireView(), article)
    }
}