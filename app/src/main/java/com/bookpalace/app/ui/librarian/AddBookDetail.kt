package com.bookpalace.app.ui.librarian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bookpalace.app.R
import com.bookpalace.app.model.Book
import com.bookpalace.app.viewmodel.BooksViewModel

class AddBookDetail : DialogFragment() {
    private var etTitle: EditText? = null
    private var etAuthor: EditText? = null
    private var etPublisher: EditText? = null
    private var etYear: EditText? = null
    private var etCategory: EditText? = null
    private var spAvailability: Spinner? = null
    private var btnAddBook: Button? = null

    private lateinit var viewModel: BooksViewModel

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_add_book_detail, container, false)

        etTitle = view.findViewById(R.id.etTitle)
        etAuthor = view.findViewById(R.id.etAuthor)
        etPublisher = view.findViewById(R.id.etPublisher)
        etYear = view.findViewById(R.id.etYear)
        etCategory = view.findViewById(R.id.etCategory)
        spAvailability = view.findViewById(R.id.spAvailability)
        btnAddBook = view.findViewById(R.id.btnAddBook)

        viewModel = ViewModelProvider(this)[BooksViewModel::class.java]

        val availability = arrayOf("Available", "Not Available")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            availability
        )
        spAvailability?.adapter = adapter

        btnAddBook?.setOnClickListener {
            addBook()
        }

        return view
    }

    private fun addBook() {
        val title = etTitle?.text.toString().trim()
        val author = etAuthor?.text.toString().trim()
        val publisher = etPublisher?.text.toString().trim()
        val year = etYear?.text.toString().trim()
        val category = etCategory?.text.toString().trim()
        val availability = spAvailability?.selectedItem.toString()

        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(context, "Please enter title and author", Toast.LENGTH_SHORT).show()
            return
        }

        val book = Book(null, title, author, publisher, year, category, availability)
        viewModel.addBook(book)

        Toast.makeText(context, "Book Added Successfully", Toast.LENGTH_SHORT).show()
        dismiss()
    }
}
