package com.bookpalace.app.model

class Book {
    var id: String? = null
    var title: String? = null
    var author: String? = null
    var publisher: String? = null
    var year: String? = null
    var category: String? = null
    var availability: String? = null

    constructor()

    constructor(
        id: String?, title: String?, author: String?, publisher: String?,
        year: String?, category: String?, availability: String?
    ) {
        this.id = id
        this.title = title
        this.author = author
        this.publisher = publisher
        this.year = year
        this.category = category
        this.availability = availability
    }
}