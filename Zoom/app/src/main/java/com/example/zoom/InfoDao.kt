package com.example.zoom

import androidx.room.*

@Dao
interface InfoDao {
    @Query("SELECT email FROM Info")
    fun getAll() : String

    @Insert
    fun insert(info:Info)

    @Update
    fun update(info:Info)

    @Query("DELETE FROM Info")
    fun deleteAll()
}