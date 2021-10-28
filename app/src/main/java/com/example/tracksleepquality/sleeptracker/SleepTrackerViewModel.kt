package com.example.tracksleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.tracksleepquality.database.SleepDatabaseDao
import com.example.tracksleepquality.database.SleepNight
import com.example.tracksleepquality.formatNights
import kotlinx.coroutines.*

class SleepTrackerViewModel(val database : SleepDatabaseDao, application: Application)
    : AndroidViewModel(application) {

    private var viewModelJob = Job()
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val tonight = MutableLiveData<SleepNight?>()
    val nights = database.getAllNight()
    // nights tn spanned 변경 for displaying
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality : LiveData<SleepNight>
        get() = _navigateToSleepQuality

    val startButtonVisible = Transformations.map(tonight) {
        null == it //  clear시 nights 에는 null => ture, 데이타 있으면 false
    }
    val stopButtonVisible = Transformations.map(tonight) {
        null != it //  clear시 nights 에는 null => false, 있으면 true
    }
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty() // clear시 nights 에는 null => false, 있으면 true
    }

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent
    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    init {
        initializeTonight()
    }
    private fun initializeTonight() {
        uiScope.launch { tonight.value = getTonigtFromDatabase() }
    }
    private suspend fun getTonigtFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonigtFromDatabase()
        }
    }
    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }
    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
        }
        _showSnackbarEvent.value = true
    }
    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }
}