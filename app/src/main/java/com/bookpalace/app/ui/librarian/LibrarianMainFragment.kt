package com.bookpalace.app.ui.librarian

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookpalace.app.adapter.BookAdapter
import com.bookpalace.app.databinding.FragmentLibrarianMainBinding
import com.bookpalace.app.viewmodel.BooksViewModel

class LibrarianMainFragment : Fragment() {

    private var _binding: FragmentLibrarianMainBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share data between fragments
    private val viewModel: BooksViewModel by activityViewModels()
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLibrarianMainBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observeBooks()
        setupSearch()
        setupFab()

        return binding.root
    }

    // RecyclerView Setup
    private fun setupRecyclerView() {

        adapter = BookAdapter(
            onUpdateClick = { book ->
                val dialog = EditBookFragment.newInstance(book)
                dialog.show(parentFragmentManager, "EditBook")
            },
            onDeleteClick = { book ->
                // Delete from both SQLite and Firebase
                book.id?.let { viewModel.deleteBook(it) }
            }
        )

        binding.recyclerViewBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewBooks.adapter = adapter
    }

    // Observe Books
    private fun observeBooks() {

        binding.progressBar.visibility = View.VISIBLE

        viewModel.allBooks.observe(viewLifecycleOwner) { books ->

            binding.progressBar.visibility = View.GONE

            if (books.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.emptyState.visibility = View.GONE
            }

            adapter.submitList(books)
        }
    }

    // Search
    private fun setupSearch() {

        binding.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {

                viewModel.searchBooks(newText ?: "")
                return true
            }
        })
    }

    // Floating Button
    private fun setupFab() {

        binding.fabAddBook.setOnClickListener {
            val dialog = AddBookDetail()
            dialog.show(parentFragmentManager, "AddBook")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
