package com.bookpalace.app.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "bookpalace.db"
        const val DATABASE_VERSION = 1

        // Table name
        const val TABLE_BOOKS = "books"

        // Column names
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_AUTHOR = "author"
        const val COL_PUBLISHER = "publisher"
        const val COL_YEAR = "year"
        const val COL_CATEGORY = "category"
        const val COL_AVAILABILITY = "availability"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_BOOKS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITLE TEXT,
                $COL_AUTHOR TEXT,
                $COL_PUBLISHER TEXT,
                $COL_YEAR TEXT,
                $COL_CATEGORY TEXT,
                $COL_AVAILABILITY TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")
        onCreate(db)
    }
}