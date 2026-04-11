package com.bookpalace.app.ui.librarian

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bookpalace.app.R
import com.bookpalace.app.model.Book
import com.bookpalace.app.viewmodel.BooksViewModel

class EditBookFragment : DialogFragment() {

    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etPublisher: EditText
    private lateinit var etYear: EditText
    private lateinit var etCategory: EditText
    private lateinit var spAvailability: Spinner
    private lateinit var btnUpdate: Button

    // Use activityViewModels to share the same instance with the main fragment and AddBookDetail
    private val booksViewModel: BooksViewModel by activityViewModels()

    private var bookId: String? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_edit_book_details, container, false)

        etTitle = view.findViewById(R.id.etTitle)
        etAuthor = view.findViewById(R.id.etAuthor)
        etPublisher = view.findViewById(R.id.etPublisher)
        etYear = view.findViewById(R.id.etYear)
        etCategory = view.findViewById(R.id.etCategory)
        spAvailability = view.findViewById(R.id.spAvailability)
        btnUpdate = view.findViewById(R.id.btnUpdateBook)

        setupSpinner()
        loadBookData()

        btnUpdate.setOnClickListener {
            updateBook()
        }

        return view
    }

    private fun setupSpinner() {
        val availabilityOptions = arrayOf("Available", "Not Available")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            availabilityOptions
        )
        spAvailability.adapter = adapter
    }

    private fun loadBookData() {
        arguments?.let {
            bookId = it.getString("id")
            etTitle.setText(it.getString("title"))
            etAuthor.setText(it.getString("author"))
            etPublisher.setText(it.getString("publisher"))
            etYear.setText(it.getString("year"))
            etCategory.setText(it.getString("category"))

            val availability = it.getString("availability")
            if (availability == "Available") {
                spAvailability.setSelection(0)
            } else {
                spAvailability.setSelection(1)
            }
        }
    }

    private fun updateBook() {
        val updatedBook = Book(
            id = bookId,
            title = etTitle.text.toString(),
            author = etAuthor.text.toString(),
            publisher = etPublisher.text.toString(),
            year = etYear.text.toString(),
            category = etCategory.text.toString(),
            availability = spAvailability.selectedItem.toString()
        )

        booksViewModel.updateBook(updatedBook)
        Toast.makeText(requireContext(), "Book Updated Successfully", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    companion object {
        fun newInstance(book: Book): EditBookFragment {
            val fragment = EditBookFragment()
            val args = Bundle().apply {
                putString("id", book.id)
                putString("title", book.title)
                putString("author", book.author)
                putString("publisher", book.publisher)
                putString("year", book.year)
                putString("category", book.category)
                putString("availability", book.availability)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
