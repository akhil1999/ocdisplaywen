package com.pruhbootlooper.test

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Profile(
    @PrimaryKey
    val profileName : String,
    @ColumnInfo(name = "P") val P : Int?,
    @ColumnInfo(name = "M") val M : Int?,
    @ColumnInfo(name = "S") val S : Int?
)