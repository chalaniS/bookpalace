package com.bookpalace.app.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bookpalace.app.database.BookDao
import com.bookpalace.app.model.Book
import com.google.firebase.database.*
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BooksViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: BooksViewModel
    private lateinit var application: Application
    private lateinit var database: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    @Before
    fun setup() {
        mockkStatic(FirebaseDatabase::class)
        firebaseDatabase = mockk(relaxed = true)
        database = mockk(relaxed = true)
        application = mockk(relaxed = true)
        
        // Mock BookDao constructor
        mockkConstructor(BookDao::class)
        // Set default behavior for any constructed BookDao
        every { anyConstructed<BookDao>().getAllBooks() } returns emptyList()

        every { FirebaseDatabase.getInstance() } returns firebaseDatabase
        every { firebaseDatabase.getReference("books") } returns database
        
        viewModel = BooksViewModel(application)
    }

    @Test
    fun `addBook should save to SQLite and Firebase`() {
        val book = Book("test_id", "Title", "Author", "Publisher", "2023", "Category", "Available")
        val pushedRef = mockk<DatabaseReference>(relaxed = true)
        
        every { database.push() } returns pushedRef
        every { pushedRef.key } returns "test_id"
        every { anyConstructed<BookDao>().insertBook(any()) } returns true
        every { anyConstructed<BookDao>().getAllBooks() } returns listOf(book)
        
        viewModel.addBook(book)
        
        // Verify SQLite insert
        verify { anyConstructed<BookDao>().insertBook(any()) }
        
        // Verify Firebase save
        verify { pushedRef.setValue(any<Book>()) }
        
        // Verify local list refresh (getAllBooks called during init and after add)
        verify(exactly = 2) { anyConstructed<BookDao>().getAllBooks() }
    }

    @Test
    fun `updateBook should update SQLite and Firebase`() {
        val book = Book("test_id", "Title", "Author", "Publisher", "2023", "Category", "Available")
        val childRef = mockk<DatabaseReference>(relaxed = true)
        
        every { database.child("test_id") } returns childRef
        every { anyConstructed<BookDao>().updateBook(any()) } returns true
        
        viewModel.updateBook(book)
        
        verify { anyConstructed<BookDao>().updateBook(any()) }
        verify { childRef.setValue(any<Book>()) }
    }

    @Test
    fun `deleteBook should delete from SQLite and Firebase`() {
        val bookId = "test_id"
        val childRef = mockk<DatabaseReference>(relaxed = true)
        
        every { database.child(bookId) } returns childRef
        every { anyConstructed<BookDao>().deleteBook(any()) } returns true
        
        viewModel.deleteBook(bookId)
        
        verify { anyConstructed<BookDao>().deleteBook(bookId) }
        verify { childRef.removeValue() }
    }

    @Test
    fun `searchBooks should filter list correctly`() {
        val book1 = Book("1", "Kotlin Guide", "Author A", "P1", "2021", "Tech", "Yes")
        val book2 = Book("2", "Java Programming", "Author B", "P2", "2020", "Tech", "Yes")
        
        // Mock the return of getAllBooks for the constructor call in setup()
        every { anyConstructed<BookDao>().getAllBooks() } returns listOf(book1, book2)
        
        // Re-initialize to trigger init block with mocked data
        viewModel = BooksViewModel(application)

        // Test search by title
        viewModel.searchBooks("Kotlin")
        assertEquals(1, viewModel.allBooks.value?.size)
        assertEquals("Kotlin Guide", viewModel.allBooks.value?.get(0)?.title)

        // Test search by author
        viewModel.searchBooks("Author B")
        assertEquals(1, viewModel.allBooks.value?.size)
        assertEquals("Java Programming", viewModel.allBooks.value?.get(0)?.title)
    }
}
