package com.bookpalace.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookpalace.app.model.Book


class BooksViewModel : ViewModel() {

    private val _allBooks = mutableListOf<Book>()

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setBooks(books: List<Book>) {
        _allBooks.clear()
        _allBooks.addAll(books)
        _books.value = books
        _isLoading.value = false
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _books.value = _allBooks
        } else {
            _books.value = _allBooks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true) ||
                        it.isbn.contains(query, ignoreCase = true)
            }
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}