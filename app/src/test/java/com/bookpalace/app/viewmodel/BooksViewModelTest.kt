package com.bookpalace.app.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
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
    private lateinit var database: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var query: DatabaseReference

    @Before
    fun setup() {
        mockkStatic(FirebaseDatabase::class)
        firebaseDatabase = mockk(relaxed = true)
        database = mockk(relaxed = true)
        query = mockk(relaxed = true)

        every { FirebaseDatabase.getInstance() } returns firebaseDatabase
        every { firebaseDatabase.getReference("books") } returns database
        
        // Initializing the ViewModel triggers observeBooks which calls addValueEventListener
        viewModel = BooksViewModel()
    }

    @Test
    fun `addBook should push to database and set value`() {
        val book = Book(null, "Title", "Author", "Publisher", "2023", "Category", "Available")
        val pushedRef = mockk<DatabaseReference>(relaxed = true)
        
        every { database.push() } returns pushedRef
        every { pushedRef.key } returns "test_id"
        
        viewModel.addBook(book)
        
        verify { pushedRef.setValue(book) }
        assertEquals("test_id", book.id)
    }

    @Test
    fun `updateBook should set value at specific child`() {
        val book = Book("test_id", "Title", "Author", "Publisher", "2023", "Category", "Available")
        val childRef = mockk<DatabaseReference>(relaxed = true)
        
        every { database.child("test_id") } returns childRef
        
        viewModel.updateBook(book)
        
        verify { childRef.setValue(book) }
    }

    @Test
    fun `deleteBook should remove value at specific child`() {
        val bookId = "test_id"
        val childRef = mockk<DatabaseReference>(relaxed = true)
        
        every { database.child(bookId) } returns childRef
        
        viewModel.deleteBook(bookId)
        
        verify { childRef.removeValue() }
    }

    @Test
    fun `searchBooks should filter list correctly`() {
        // We need to simulate data change to populate fullBookList
        val slot = slot<ValueEventListener>()
        verify { database.addValueEventListener(capture(slot)) }

        val snapshot = mockk<DataSnapshot>()
        val child1 = mockk<DataSnapshot>()
        val child2 = mockk<DataSnapshot>()
        
        val book1 = Book("1", "Kotlin Guide", "Author A", "P1", "2021", "Tech", "Yes")
        val book2 = Book("2", "Java Programming", "Author B", "P2", "2020", "Tech", "Yes")

        every { snapshot.children } returns listOf(child1, child2)
        every { child1.getValue(Book::class.java) } returns book1
        every { child2.getValue(Book::class.java) } returns book2

        slot.captured.onDataChange(snapshot)

        // Test search by title
        viewModel.searchBooks("Kotlin")
        assertEquals(1, viewModel.allBooks.value?.size)
        assertEquals("Kotlin Guide", viewModel.allBooks.value?.get(0)?.title)

        // Test search by author
        viewModel.searchBooks("Author B")
        assertEquals(1, viewModel.allBooks.value?.size)
        assertEquals("Java Programming", viewModel.allBooks.value?.get(0)?.title)

        // Test empty query (should return all)
        viewModel.searchBooks("")
        assertEquals(2, viewModel.allBooks.value?.size)
    }
}
