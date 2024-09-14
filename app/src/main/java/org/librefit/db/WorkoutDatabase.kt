package org.librefit.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Workout::class, Exercise::class], version = 1, exportSchema = false)
abstract class WorkoutDatabase : RoomDatabase(){
    companion object{
        const val NAME = "workout_database"
    }

    abstract fun getWorkoutDao() : WorkoutDao
}