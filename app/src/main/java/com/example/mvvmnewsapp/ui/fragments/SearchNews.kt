package com.example.mvvmnewsapp.ui.fragments

import android.app.DatePickerDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmnewsapp.R
import com.example.mvvmnewsapp.adapters.NewsAdapter
import com.example.mvvmnewsapp.ui.NewsActivity
import com.example.mvvmnewsapp.ui.NewsViewModel
import com.example.mvvmnewsapp.util.Constants
import com.example.mvvmnewsapp.util.Constants.SEARCH_NEWS_TIME_DELAY
import com.example.mvvmnewsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "SearchNewsFragment"

class SearchNews : Fragment(R.layout.fragment_search_news) {
    private lateinit var newsAdapter: NewsAdapter
    private var lastQuery = ""
    lateinit var viewModel: NewsViewModel
    private var job: Job? = null
    private var filterFlag = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = (activity as NewsActivity).viewModel
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setHasOptionsMenu(true)
        (activity as NewsActivity).apply {
            setBNVVisible(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }


        etFrom.setOnClickListener { setDate(it) }
        etTo.setOnClickListener { setDate(it) }




        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_searchNews_to_article, bundle)
        }


        viewModel.searchNews.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {

                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)
                        if (newsResponse.articles.size == 0) {
                            Toast.makeText(
                                activity,
                                "No news found for keyword: $lastQuery, please be more specific",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPage == totalPages
                        Log.d(TAG, "Total results: ${newsResponse.totalResults}")
                        hideProgressBar()
                        if (isLastPage) {
                            rvSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }

                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {

                    showProgressBar()
                }
            }
        })

    }


    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true

    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE

            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getSearchNews(
                    "",
                    from = etFrom.text.toString(),
                    to = etTo.text.toString()
                )
                isScrolling = false
            }
        }
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()

        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNews.scrollListener)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.search_menu, menu)

        val manager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.search)
        val searchView =
            searchItem?.actionView as SearchView

        searchView.setSearchableInfo(manager.getSearchableInfo(activity?.componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()

                handleSearchQuery(query)

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                handleSearchQuery(newText)
                return false
            }
        })


        super.onCreateOptionsMenu(menu, inflater)
    }

    fun handleSearchQuery(query: String?) {
        job?.cancel()

        job = MainScope().launch {
            delay(SEARCH_NEWS_TIME_DELAY)

            if (query != lastQuery) {
                query?.let {
                    if (it.isNotEmpty()) {
                        viewModel.resetResults()
                        viewModel.getSearchNews(
                            it,
                            from = etFrom.text.toString(),
                            to = etTo.text.toString()
                        )
                    }
                    lastQuery = query
                }
            }


        }
    }

    private fun setDate(view: View) {

        val et = view as EditText
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val cal = Calendar.getInstance()


        if (et.text.toString().isNotEmpty()) {
            sdf.parse(et.text.toString())?.let {
                cal.timeInMillis = it.time
            }

        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        Log.d(TAG, "month:$month, year:$year, day:$day")
        val dpd = DatePickerDialog(
            activity as Context,
            { _, y, monthOfYear, dayOfMonth ->
                val c = GregorianCalendar(y, monthOfYear, dayOfMonth).time
                val date = sdf.format(c)
                et.setText(date)


                viewModel.resetResults()
                viewModel.getSearchNews(
                    "",
                    from = etFrom.text.toString(),
                    to = etTo.text.toString()
                )
            },
            year,
            month,
            day
        )


        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.datePicker.minDate = System.currentTimeMillis() - 86400L * 30 * 1000
        dpd.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.Filter) {
            if (filterFlag) {
                item.title = "Filter"
                Filter.visibility = View.GONE
                etTo.setText("")
                etFrom.setText("")
            } else {
                item.title = "Remove Filter"
                Filter.visibility = View.VISIBLE
            }

            filterFlag = !filterFlag
        }
        return super.onOptionsItemSelected(item)
    }
}

