package com.example.newsapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapp.Result


@Dao
interface ResultDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(result: Result): Long
    @Query("SELECT * FROM results")
    fun getAllResults(): LiveData<List<Result>>
    @Delete
    suspend fun deleteResult(result: Result)
}