package com.bookpalace.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookpalace.app.R
import com.bookpalace.app.model.Book

class BookAdapter(
    private val onItemClick: (Book) -> Unit = {}
) : ListAdapter<Book, BookAdapter.BookViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textTitle: TextView = itemView.findViewById(R.id.bookTitle)
        private val textAuthor: TextView = itemView.findViewById(R.id.bookAuthor)
        private val textAvailable: TextView = itemView.findViewById(R.id.bookAvailability)
        private val textPublisher: TextView = itemView.findViewById(R.id.bookPublisher)
        private val textBookYear: TextView = itemView.findViewById(R.id.bookYear)
        private val textCategory: TextView = itemView.findViewById(R.id.bookCategory)

        fun bind(book: Book) {

            textTitle.text = book.title
            textAuthor.text = book.author
            textAvailable.text = "Available: ${book.availableCopies}/${book.totalCopies}"
            textPublisher.text = "Publisher: ${book.publisher}"
            textBookYear.text = "Year: ${book.publishedYear}"
            textCategory.text = "Category: ${book.categoryId}"

            itemView.setOnClickListener {
                onItemClick(book)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}