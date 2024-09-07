package com.example.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Resource
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentHeadlinesBinding
import com.example.newsapp.databinding.ItemErrorBinding

class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {

    private lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentHeadlinesBinding
    private lateinit var errorBinding: ItemErrorBinding

    private lateinit var newsViewModel: NewsViewModel
    private var isError = false
    private var isLoading = false
    private var isScrolling = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHeadlinesBinding.bind(view)
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view:View = inflater.inflate(R.layout.item_error,null)
        // Inflate the error layout
        errorBinding = ItemErrorBinding.bind(binding.root.findViewById(R.id.itemHeadlinesError))
        newsViewModel = (activity as NewsActivity).newsViewModel
        setupHeadlinesRecycler()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("Result",it)

            }
            findNavController().navigate(R.id.action_headlinesFragment_to_articleFragment,bundle)

        }

        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.results.toList())
                    }
                }
                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "Error: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }
        })

        // Set up the retry button
        errorBinding.retryButton.setOnClickListener {
            newsViewModel.getHeadlines("vi")
        }
    }

    private fun setupHeadlinesRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val isTotalMoreThanVisible = totalItemCount >= visibleItemCount
                    val isNotAtBeginning = firstVisibleItemPosition >= 0
                    val isAtLastItem = (visibleItemCount + firstVisibleItemPosition >= totalItemCount)
                    val shouldPaginate = isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && !isLoading && !isError
                    if (shouldPaginate) {
                        isScrolling = false
                        newsViewModel.getHeadlines("vi")
                    } else {
                        isScrolling = true
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        isScrolling = true
                    }
                }
            })
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        errorBinding.root.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        errorBinding.root.visibility = View.VISIBLE
        errorBinding.errorText.text = message
        isError = true
    }
}