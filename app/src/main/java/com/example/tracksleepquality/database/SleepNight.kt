package com.example.tracksleepquality.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_sleep_quality_table")
data class SleepNight (
    @PrimaryKey(autoGenerate = true)
    var nightId: Long = 0L,

    @ColumnInfo(name = "start_time_milli")
    val startTimeMilli: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "end_time_milli")
    var endTimeMilli: Long = startTimeMilli,

    @ColumnInfo(name = "quality_rating")
    var sleepQuality: Int = -1
)
/*
    constructor(nightId: Long, startTimeMilli : Long, endTimeMilli: Long, sleepQuality: Int) {
        this.nightId = nightId
        this.startTimeMilli = System.currentTimeMillis()
        this.endTimeMilli = endTimeMilli
        this.sleepQuality = sleepQuality
    }*/


