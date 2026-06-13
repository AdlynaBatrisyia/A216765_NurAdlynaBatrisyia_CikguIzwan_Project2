package com.example.a216765_wan_lab1

import kotlinx.coroutines.flow.Flow

class ActivityRepository(private val dao: ActivityDao) {

    val allActivities: Flow<List<ActivityEntity>> = dao.getAll()

    suspend fun insert(activity: ActivityEntity) {
        dao.insert(activity)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun deleteById(id: Int) {
        dao.deleteById(id)
    }
}

