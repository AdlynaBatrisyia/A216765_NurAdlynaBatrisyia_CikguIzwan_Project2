package com.example.a216765_wan_lab1

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_table")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val time: String,
    val type: String  // "mood", "gratitude", "sleep", "simple", "mental_health"
)