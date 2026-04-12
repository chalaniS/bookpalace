package com.bookpalace.app.ui.librarian

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookpalace.app.adapter.StudentNotificationAdapter
import com.bookpalace.app.databinding.FragmentSendNotificationBinding
import com.bookpalace.app.repositories.NotificationResult
import com.bookpalace.app.viewmodel.SendNotificationViewModel
import com.bookpalace.app.viewmodel.StudentSelectionItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SendNotificationFragment : Fragment() {

    private var _binding: FragmentSendNotificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SendNotificationViewModel by viewModels()

    private lateinit var studentAdapter: StudentNotificationAdapter

    // ---------- Lifecycle ----------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupInputListeners()
        setupClickListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------- Setup ----------

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentNotificationAdapter { studentId, isChecked ->
            viewModel.onStudentChecked(studentId, isChecked)
        }

        binding.rvStudents.apply {
            adapter = studentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
        }
    }

    private fun setupInputListeners() {
        // Title
        binding.etTitle.doAfterTextChanged { text ->
            viewModel.onTitleChanged(text?.toString() ?: "")
        }

        // Message body
        binding.etMessage.doAfterTextChanged { text ->
            viewModel.onMessageChanged(text?.toString() ?: "")
        }

        // Search
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString() ?: "")
        }
    }

    private fun setupClickListeners() {
        // Select All checkbox
        binding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onSelectAllToggled(isChecked)
        }

        // Send FAB
        binding.fabSend.setOnClickListener {
            confirmAndSend()
        }
    }

    // ---------- Observe State ----------

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderStudentList(state.filteredStudents)
                    renderSelectedCount(state.selectedCount)
                    renderSelectAllCheckbox(state.allSelected)
                    renderLoading(state.isLoadingStudents, state.isSending)
                    renderFabState(state.canSend)
                    renderEmptyState(state.filteredStudents.isEmpty() && !state.isLoadingStudents)

                    // Handle errors
                    state.error?.let { error ->
                        showSnackbar(error)
                        viewModel.clearError()
                    }

                    // Handle send result
                    state.sendResult?.let { result ->
                        handleSendResult(result)
                        viewModel.clearSendResult()
                    }
                }
            }
        }
    }

    // ---------- Render helpers ----------

    private fun renderStudentList(items: List<StudentSelectionItem>) {
        studentAdapter.submitList(items)
    }

    private fun renderSelectedCount(count: Int) {
        binding.tvSelectedCount.text = "$count student${if (count != 1) "s" else ""} selected"
    }

    private fun renderSelectAllCheckbox(allSelected: Boolean) {
        // Prevent triggering listener during programmatic update
        binding.cbSelectAll.setOnCheckedChangeListener(null)
        binding.cbSelectAll.isChecked = allSelected
        binding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onSelectAllToggled(isChecked)
        }
    }

    private fun renderLoading(isLoadingStudents: Boolean, isSending: Boolean) {
        binding.loadingOverlay.visibility = if (isSending) View.VISIBLE else View.GONE
        // Optionally show shimmer for student list loading
        binding.rvStudents.alpha = if (isLoadingStudents) 0.4f else 1.0f
    }

    private fun renderFabState(canSend: Boolean) {
        binding.fabSend.isEnabled = canSend
        binding.fabSend.alpha = if (canSend) 1.0f else 0.5f
    }

    private fun renderEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvStudents.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    // ---------- Send confirmation dialog ----------

    private fun confirmAndSend() {
        val state = viewModel.uiState.value
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Send Notification")
            .setMessage(
                "Send \"${state.title}\" to ${state.selectedCount} student(s)?"
            )
            .setPositiveButton("Send") { _, _ -> viewModel.sendNotification() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------- Result handling ----------

    private fun handleSendResult(result: NotificationResult) {
        when (result) {
            is NotificationResult.Success -> {
                showSnackbar("✓ Notification sent to ${result.sentCount} student(s).")
                clearForm()
            }
            is NotificationResult.PartialSuccess -> {
                showSnackbar(
                    "Sent: ${result.sentCount}, Failed: ${result.failedCount}",
                    isError = true
                )
            }
            is NotificationResult.Failure -> {
                showSnackbar("Failed: ${result.error}", isError = true)
            }
        }
    }

    private fun clearForm() {
        binding.etTitle.setText("")
        binding.etMessage.setText("")
        binding.etSearch.setText("")
        viewModel.onSelectAllToggled(false)
    }

    // ---------- Snackbar ----------

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(requireContext().getColor(R.color.holo_red_dark))
        }
        snackbar.show()
    }
}