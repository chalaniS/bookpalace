package com.bookpalace.app.repositories

import androidx.lifecycle.LiveData
import com.bookpalace.app.model.Book

interface BookRepository {

    fun addBook(book: Book)

    fun getBooks(): LiveData<List<Book>>

    fun updateBook(book: Book)

    fun deleteBook(bookId: String)
}