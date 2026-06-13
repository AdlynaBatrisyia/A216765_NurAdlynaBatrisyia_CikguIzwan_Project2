package com.example.a216765_wan_lab1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)

    @Query("SELECT * FROM activity_table ORDER BY id DESC")
    fun getAll(): Flow<List<ActivityEntity>>

    @Query("DELETE FROM activity_table")
    suspend fun deleteAll()

    @Query("DELETE FROM activity_table WHERE id = :id")
    suspend fun deleteById(id: Int)
}

