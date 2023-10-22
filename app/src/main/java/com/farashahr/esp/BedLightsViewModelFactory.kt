package com.farashahr.esp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BedLightsViewModelFactory(
    private val queryDeliveryUrl: String
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        BedLightsViewModel(queryDeliveryUrl) as T
}