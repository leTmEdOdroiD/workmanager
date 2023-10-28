package com.dew.workmanager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class ImageCompressionViewModel : ViewModel() {
    var workerID = MutableLiveData<UUID?>(null)
}