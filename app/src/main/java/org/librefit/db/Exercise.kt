package org.librefit.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class Exercise(
    @PrimaryKey(true) val id: Int = 0,
    val exerciseId : String,
    val notes: String? = null,
    val workoutId: Int // Foreign key reference to Workout
)
