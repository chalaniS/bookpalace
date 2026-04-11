package com.bookpalace.app.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bookpalace.app.model.Book
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var bookDao: BookDao

    @Before
    fun setup() {
        // Use ApplicationProvider to get context for BookDao
        bookDao = BookDao(ApplicationProvider.getApplicationContext())
        
        // Clear database before each test
        val books = bookDao.getAllBooks()
        books.forEach { bookDao.deleteBook(it.id!!) }
    }

    @Test
    fun insertAndGetBook() {
        val book = Book("1", "Title", "Author", "Pub", "2023", "Cat", "Available")
        val result = bookDao.insertBook(book)
        
        assertTrue(result)
        
        val retrieved = bookDao.getBookById("1")
        assertNotNull(retrieved)
        assertEquals("Title", retrieved?.title)
    }

    @Test
    fun updateBook() {
        val book = Book("1", "Original Title", "Author", "Pub", "2023", "Cat", "Available")
        bookDao.insertBook(book)
        
        val updatedBook = Book("1", "Updated Title", "Author", "Pub", "2023", "Cat", "Available")
        val result = bookDao.updateBook(updatedBook)
        
        assertTrue(result)
        
        val retrieved = bookDao.getBookById("1")
        assertEquals("Updated Title", retrieved?.title)
    }

    @Test
    fun deleteBook() {
        val book = Book("1", "Title", "Author", "Pub", "2023", "Cat", "Available")
        bookDao.insertBook(book)
        
        val result = bookDao.deleteBook("1")
        assertTrue(result)
        
        val retrieved = bookDao.getBookById("1")
        assertNull(retrieved)
    }

    @Test
    fun getAllBooks() {
        val book1 = Book("1", "Title 1", "Author 1", "Pub", "2023", "Cat", "Available")
        val book2 = Book("2", "Title 2", "Author 2", "Pub", "2023", "Cat", "Available")
        
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)
        
        val allBooks = bookDao.getAllBooks()
        assertEquals(2, allBooks.size)
    }
}
