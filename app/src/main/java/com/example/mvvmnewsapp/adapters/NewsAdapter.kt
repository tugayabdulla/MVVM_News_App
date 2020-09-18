package com.example.mvvmnewsapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmnewsapp.R
import com.example.mvvmnewsapp.models.ArticlesItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_article_preview.view.*


class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

class NewsAdapter : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article_preview, parent, false)
        Log.d("Adapter", "onCreateViewHolder called")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val article = differ.currentList[position]
        holder.itemView.apply {

            if (article.urlToImage.isNullOrEmpty()) {
                Picasso.get().load(R.drawable.no_image_foreground)
                    .fit()
                    .centerInside().into(ivArticleImage)
            } else {
                Picasso.get().load(article.urlToImage)
                    .fit()
                    .centerInside().into(ivArticleImage)
            }

            tvSource.text = article.source!!.name
            tvTitle.text = article.title
            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt

            setOnClickListener {
                onItemClickListener?.let { it(article) }
            }

        }

    }

    private var onItemClickListener: ((ArticlesItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (ArticlesItem) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val differCallback = object : DiffUtil.ItemCallback<ArticlesItem>() {
        override fun areItemsTheSame(oldItem: ArticlesItem, newItem: ArticlesItem): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: ArticlesItem, newItem: ArticlesItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

}