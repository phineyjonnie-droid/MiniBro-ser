package com.veestores.minibrowser

data class Tab(val id: String, var title: String = "", var url: String = "about:blank", var isIncognito: Boolean = false, var thumbPath: String = "")
