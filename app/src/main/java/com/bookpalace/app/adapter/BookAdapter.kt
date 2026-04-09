package com.bookpalace.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookpalace.app.R
import com.bookpalace.app.model.Book

class BookAdapter(
    private val onUpdateClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book, onUpdateClick, onDeleteClick)
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.bookTitle)
        private val author: TextView = itemView.findViewById(R.id.bookAuthor)
        private val publisher: TextView = itemView.findViewById(R.id.bookPublisher)
        private val year: TextView = itemView.findViewById(R.id.bookYear)
        private val category: TextView = itemView.findViewById(R.id.bookCategory)
        private val availability: TextView = itemView.findViewById(R.id.bookAvailability)
        private val btnUpdate: Button = itemView.findViewById(R.id.btnUpdate)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(book: Book, onUpdateClick: (Book) -> Unit, onDeleteClick: (Book) -> Unit) {
            title.text = book.title ?: ""
            author.text = book.author ?: ""
            publisher.text = book.publisher ?: ""
            year.text = book.year ?: ""
            category.text = book.category ?: ""
            availability.text = book.availability ?: ""

            btnUpdate.setOnClickListener { onUpdateClick(book) }
            btnDelete.setOnClickListener { onDeleteClick(book) }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.title == newItem.title &&
                    oldItem.author == newItem.author &&
                    oldItem.publisher == newItem.publisher &&
                    oldItem.year == newItem.year &&
                    oldItem.category == newItem.category &&
                    oldItem.availability == newItem.availability
        }
    }
}
