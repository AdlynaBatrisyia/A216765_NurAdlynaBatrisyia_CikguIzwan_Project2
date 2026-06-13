package com.example.a216765_wan_lab1

import android.app.Application

class MoodSpaceApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    val repository: ActivityRepository by lazy {
        ActivityRepository(database.activityDao())
    }
}

