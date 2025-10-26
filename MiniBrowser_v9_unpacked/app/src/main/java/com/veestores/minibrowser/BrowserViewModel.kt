package com.veestores.minibrowser

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

class BrowserViewModel : ViewModel() {
    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadUrl(url: String) {
        _currentUrl.value = url
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}
