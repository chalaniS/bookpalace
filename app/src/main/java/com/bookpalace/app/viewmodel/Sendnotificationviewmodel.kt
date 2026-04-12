package com.bookpalace.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookpalace.app.model.User
import com.bookpalace.app.repositories.NotificationPayload
import com.bookpalace.app.repositories.NotificationRepository
import com.bookpalace.app.repositories.NotificationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------- UI State ----------

data class SendNotificationUiState(
    val isLoadingStudents: Boolean = false,
    val isSending: Boolean = false,
    val students: List<StudentSelectionItem> = emptyList(),
    val filteredStudents: List<StudentSelectionItem> = emptyList(),
    val title: String = "",
    val message: String = "",
    val searchQuery: String = "",
    val error: String? = null,
    val sendResult: NotificationResult? = null
) {
    val selectedStudents: List<StudentSelectionItem>
        get() = students.filter { it.isSelected }

    val selectedCount: Int get() = selectedStudents.size

    val allSelected: Boolean
        get() = students.isNotEmpty() && students.all { it.isSelected }

    val canSend: Boolean
        get() = title.isNotBlank() && message.isNotBlank() && selectedCount > 0 && !isSending
}

data class StudentSelectionItem(
    val user: User,
    val isSelected: Boolean = false
)

// ---------- ViewModel ----------

@HiltViewModel
class SendNotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendNotificationUiState())
    val uiState: StateFlow<SendNotificationUiState> = _uiState.asStateFlow()

    // Separate flow to debounce search input
    private val _searchQuery = MutableStateFlow("")

    init {
        loadStudents()
        observeSearch()
    }

    // ---------- Load ----------

    fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStudents = true, error = null) }

            repository.fetchAllStudents()
                .onSuccess { users ->
                    val items = users.map { StudentSelectionItem(it) }
                    _uiState.update {
                        it.copy(
                            isLoadingStudents = false,
                            students = items,
                            filteredStudents = items
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingStudents = false,
                            error = e.message ?: "Failed to load students."
                        )
                    }
                }
        }
    }

    // ---------- Input events ----------

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onMessageChanged(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onStudentChecked(studentId: String, isChecked: Boolean) {
        _uiState.update { state ->
            val updated = state.students.map { item ->
                if (item.user.id == studentId) item.copy(isSelected = isChecked) else item
            }
            state.copy(
                students = updated,
                filteredStudents = applyFilter(updated, state.searchQuery)
            )
        }
    }

    fun onSelectAllToggled(selectAll: Boolean) {
        _uiState.update { state ->
            val updated = state.students.map { it.copy(isSelected = selectAll) }
            state.copy(
                students = updated,
                filteredStudents = applyFilter(updated, state.searchQuery)
            )
        }
    }

    // ---------- Search ----------

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest { query ->
                    _uiState.update { state ->
                        state.copy(
                            filteredStudents = applyFilter(state.students, query)
                        )
                    }
                }
        }
    }

    private fun applyFilter(
        items: List<StudentSelectionItem>,
        query: String
    ): List<StudentSelectionItem> {
        if (query.isBlank()) return items
        val lower = query.lowercase()
        return items.filter {
            it.user.name.lowercase().contains(lower) ||
                    it.user.email.lowercase().contains(lower)
        }
    }

    // ---------- Send ----------

    fun sendNotification() {
        val state = _uiState.value
        if (!state.canSend) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null, sendResult = null) }

            val payload = NotificationPayload(
                title = state.title.trim(),
                message = state.message.trim(),
                recipients = state.selectedStudents.map { it.user }
            )

            val result = repository.sendNotification(payload)

            _uiState.update {
                it.copy(
                    isSending = false,
                    sendResult = result,
                    error = if (result is NotificationResult.Failure) result.error else null
                )
            }
        }
    }

    fun clearSendResult() {
        _uiState.update { it.copy(sendResult = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
