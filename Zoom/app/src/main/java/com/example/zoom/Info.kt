package com.example.zoom

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Info(val email:String,val type:Int) {
    @PrimaryKey(autoGenerate = true) var id:Int=0
}