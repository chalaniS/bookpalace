package com.bookpalace.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookpalace.app.model.Book
import com.bookpalace.app.repositories.BookRepository
import com.google.firebase.database.*

class BooksViewModel : ViewModel(), BookRepository {

    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("books")

    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> get() = _allBooks

    private var fullBookList = listOf<Book>()

    init {
        observeBooks()
    }

    private fun observeBooks() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookList = mutableListOf<Book>()
                for (data in snapshot.children) {
                    val book = data.getValue(Book::class.java)
                    book?.let { bookList.add(it) }
                }
                fullBookList = bookList
                _allBooks.value = bookList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getBooks(): LiveData<List<Book>> = allBooks

    override fun addBook(book: Book) {
        val id = database.push().key
        book.id = id
        id?.let {
            database.child(it).setValue(book)
        }
    }

    override fun updateBook(book: Book) {
        book.id?.let {
            database.child(it).setValue(book)
        }
    }

    override fun deleteBook(bookId: String) {
        database.child(bookId).removeValue()
    }

    fun searchBooks(query: String) {
        if (query.isEmpty()) {
            _allBooks.value = fullBookList
        } else {
            val filteredList = fullBookList.filter {
                it.title?.contains(query, ignoreCase = true) == true ||
                it.author?.contains(query, ignoreCase = true) == true
            }
            _allBooks.value = filteredList
        }
    }
}
