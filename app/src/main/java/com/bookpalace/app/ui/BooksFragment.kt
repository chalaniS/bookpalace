package com.bookpalace.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookpalace.app.R
import com.bookpalace.app.model.Book
import com.bookpalace.app.adapter.BookAdapter
import java.time.LocalDateTime

class BooksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private val bookList = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_book_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadBooks()

        adapter = BookAdapter(bookList as (Book) -> Unit)
        recyclerView.adapter = adapter

        return view
    }

    private fun loadBooks() {
        bookList.add(
            Book(
                id = "1",
                title = "Clean Code",
                author = "Robert Martin",
                isbn = "9780132350884",
                categoryId = "programming",
                publisher = "Prentice Hall",
                publishedYear = 2008,
                totalCopies = 5,
                availableCopies = 3,
                location = "Shelf A1",
                addedBy = "admin",
                addedAt = LocalDateTime.now()
            )
        )
        bookList.add(
            Book(
                id = "2",
                title = "Atomic Habits",
                author = "James Clear",
                isbn = "9780735211292",
                categoryId = "selfhelp",
                publisher = "Avery",
                publishedYear = 2018,
                totalCopies = 4,
                availableCopies = 2,
                location = "Shelf B2",
                addedBy = "admin",
                addedAt = LocalDateTime.now()
            )
        )
    }
}