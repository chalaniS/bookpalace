package com.bookpalace.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bookpalace.app.database.BookDao
import com.bookpalace.app.model.Book
import com.bookpalace.app.repositories.BookRepository
import com.google.firebase.database.*

class BooksViewModel(application: Application) : AndroidViewModel(application), BookRepository {

    private val bookDao = BookDao(application)
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("books")

    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> get() = _allBooks

    private var fullBookList = listOf<Book>()

    init {
        observeBooks()
    }

    private fun observeBooks() {
        // Load offline data initially
        fullBookList = bookDao.getAllBooks()
        _allBooks.value = fullBookList

        // Sync with Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookList = mutableListOf<Book>()
                for (data in snapshot.children) {
                    val book = data.getValue(Book::class.java)
                    book?.let { bookList.add(it) }
                }
                
                // If Firebase has data, use it and update local cache if needed
                if (bookList.isNotEmpty()) {
                    fullBookList = bookList
                    _allBooks.value = bookList
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getBooks(): LiveData<List<Book>> = allBooks

    override fun addBook(book: Book) {
        // Generate a unique ID using Firebase push key
        val pushedRef = database.push()
        val id = pushedRef.key ?: System.currentTimeMillis().toString()
        book.id = id

        // 1. Save to SQLite for offline access
        bookDao.insertBook(book)

        // 2. Save to Firebase for online sync
        pushedRef.setValue(book)

        // 3. Update local list immediately
        refreshLocalBooks()
    }

    override fun updateBook(book: Book) {
        book.id?.let { id ->
            // 1. Update SQLite
            bookDao.updateBook(book)
            
            // 2. Update Firebase
            database.child(id).setValue(book)
            
            // 3. Update local list
            refreshLocalBooks()
        }
    }

    override fun deleteBook(bookId: String) {
        // 1. Delete from SQLite
        bookDao.deleteBook(bookId)
        
        // 2. Delete from Firebase
        database.child(bookId).removeValue()
        
        // 3. Update local list
        refreshLocalBooks()
    }

    private fun refreshLocalBooks() {
        fullBookList = bookDao.getAllBooks()
        _allBooks.value = fullBookList
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
