package com.example.heatmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "track",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            childColumns = ["trip_id"],
            parentColumns = ["id"]
        ),
        ForeignKey(
            entity = Car::class,
            childColumns = ["car_id"],
            parentColumns = ["id"]
        )
    ]
)
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "trip_id") val tripId: Long,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "number") val number: Int,
    @ColumnInfo(name = "start") val start: String,
    @ColumnInfo(name = "end") val end: String,
    @ColumnInfo(name = "car_id") val carId: Int
)
