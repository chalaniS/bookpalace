package com.bookpalace.app.database

import android.content.ContentValues
import android.content.Context
import com.bookpalace.app.model.Book


//Dao = Data Access Object
class BookDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // INSERT a new book
    fun insertBook(book: Book): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ID, book.id)
            put(DatabaseHelper.COL_TITLE, book.title)
            put(DatabaseHelper.COL_AUTHOR, book.author)
            put(DatabaseHelper.COL_PUBLISHER, book.publisher)
            put(DatabaseHelper.COL_YEAR, book.year)
            put(DatabaseHelper.COL_CATEGORY, book.category)
            put(DatabaseHelper.COL_AVAILABILITY, book.availability)
        }
        val result = db.insert(DatabaseHelper.TABLE_BOOKS, null, values)
        db.close()
        return result != -1L
    }

    // GET all books
    fun getAllBooks(): List<Book> {
        val books = mutableListOf<Book>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_BOOKS}", null)
        if (cursor.moveToFirst()) {
            do {
                val book = Book(
                    id           = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                    title        = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE)),
                    author       = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AUTHOR)),
                    publisher    = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PUBLISHER)),
                    year         = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_YEAR)),
                    category     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY)),
                    availability = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVAILABILITY))
                )
                books.add(book)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return books
    }

    // GET book by ID
    fun getBookById(id: String): Book? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_BOOKS,
            null,
            "${DatabaseHelper.COL_ID} = ?",
            arrayOf(id),
            null, null, null
        )
        var book: Book? = null
        if (cursor.moveToFirst()) {
            book = Book(
                id           = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                title        = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE)),
                author       = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AUTHOR)),
                publisher    = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PUBLISHER)),
                year         = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_YEAR)),
                category     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY)),
                availability = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVAILABILITY))
            )
        }
        cursor.close()
        db.close()
        return book
    }

    // UPDATE a book
    fun updateBook(book: Book): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TITLE, book.title)
            put(DatabaseHelper.COL_AUTHOR, book.author)
            put(DatabaseHelper.COL_PUBLISHER, book.publisher)
            put(DatabaseHelper.COL_YEAR, book.year)
            put(DatabaseHelper.COL_CATEGORY, book.category)
            put(DatabaseHelper.COL_AVAILABILITY, book.availability)
        }
        val result = db.update(
            DatabaseHelper.TABLE_BOOKS,
            values,
            "${DatabaseHelper.COL_ID} = ?",
            arrayOf(book.id)
        )
        db.close()
        return result > 0
    }

    // DELETE a book by ID
    fun deleteBook(id: String): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete(
            DatabaseHelper.TABLE_BOOKS,
            "${DatabaseHelper.COL_ID} = ?",
            arrayOf(id)
        )
        db.close()
        return result > 0
    }
}