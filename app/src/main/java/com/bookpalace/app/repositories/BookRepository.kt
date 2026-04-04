package com.bookpalace.app.repositories

import com.bookpalace.app.model.Book


interface BookRepository {
    fun findById(id: String): Book?
    fun findAll(): List<Book>
    fun findByTitle(title: String): List<Book>
    fun findByAuthor(author: String): List<Book>
    fun findByIsbn(isbn: String): Book?
    fun findByCategoryId(categoryId: String): List<Book>
    fun findAvailable(): List<Book>
    fun search(query: String): List<Book>
    fun save(book: Book): Book
    fun update(book: Book): Book
    fun softDelete(id: String): Boolean
    fun updateAvailableCopies(id: String, delta: Int): Boolean
    fun existsByIsbn(isbn: String): Boolean
}
