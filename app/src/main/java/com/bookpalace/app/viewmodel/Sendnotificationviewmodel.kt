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
    val isLoadingRecipients: Boolean = false,
    val isSending: Boolean = false,
    val recipients: List<RecipientSelectionItem> = emptyList(),
    val filteredRecipients: List<RecipientSelectionItem> = emptyList(),
    val title: String = "",
    val message: String = "",
    val searchQuery: String = "",
    val error: String? = null,
    val sendResult: NotificationResult? = null
) {
    val selectedRecipients: List<RecipientSelectionItem>
        get() = recipients.filter { it.isSelected }

    val selectedCount: Int get() = selectedRecipients.size

    val allSelected: Boolean
        get() = recipients.isNotEmpty() && recipients.all { it.isSelected }

    val canSend: Boolean
        get() = title.isNotBlank() && message.isNotBlank() && selectedCount > 0 && !isSending
}

data class RecipientSelectionItem(
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
        loadRecipients()
        observeSearch()
    }

    // ---------- Load ----------

    fun loadRecipients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRecipients = true, error = null) }

            repository.fetchAllRecipients()
                .onSuccess { users ->
                    val items = users.map { RecipientSelectionItem(it) }
                    _uiState.update {
                        it.copy(
                            isLoadingRecipients = false,
                            recipients = items,
                            filteredRecipients = items
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingRecipients = false,
                            error = e.message ?: "Failed to load recipients."
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

    fun onRecipientChecked(userId: String, isChecked: Boolean) {
        _uiState.update { state ->
            val updated = state.recipients.map { item ->
                if (item.user.id == userId) item.copy(isSelected = isChecked) else item
            }
            state.copy(
                recipients = updated,
                filteredRecipients = applyFilter(updated, state.searchQuery)
            )
        }
    }

    fun onSelectAllToggled(selectAll: Boolean) {
        _uiState.update { state ->
            val updated = state.recipients.map { it.copy(isSelected = selectAll) }
            state.copy(
                recipients = updated,
                filteredRecipients = applyFilter(updated, state.searchQuery)
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
                            filteredRecipients = applyFilter(state.recipients, query)
                        )
                    }
                }
        }
    }

    private fun applyFilter(
        items: List<RecipientSelectionItem>,
        query: String
    ): List<RecipientSelectionItem> {
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
                recipients = state.selectedRecipients.map { it.user }
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
