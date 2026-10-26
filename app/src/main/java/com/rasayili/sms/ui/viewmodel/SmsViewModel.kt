package com.rasayili.sms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rasayili.sms.data.model.Conversation
import com.rasayili.sms.data.model.SmsMessage
import com.rasayili.sms.data.repository.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SmsRepository(application.applicationContext)
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    private val _currentThreadMessages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val currentThreadMessages: StateFlow<List<SmsMessage>> = _currentThreadMessages.asStateFlow()
    private val _isDefaultSmsApp = MutableStateFlow(false)
    val isDefaultSmsApp: StateFlow<Boolean> = _isDefaultSmsApp.asStateFlow()
    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onDefaultSmsAppStatusChanged(isDefault: Boolean) { _isDefaultSmsApp.value = isDefault }
    fun onPermissionsResult(granted: Boolean) { _hasPermissions.value = granted }
    fun checkPermissions(granted: Boolean) { _hasPermissions.value = granted }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun loadConversations() { viewModelScope.launch { _conversations.value = repository.getConversations() } }
    fun loadMessagesForThread(threadId: Long) { viewModelScope.launch { _currentThreadMessages.value = repository.getMessagesForThread(threadId) } }
    fun sendSms(address: String, body: String, subscriptionId: Int? = null) {
        viewModelScope.launch {
            repository.sendSms(address, body, subscriptionId)
            loadConversations()
        }
    }
}