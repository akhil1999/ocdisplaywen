package com.pruhbootlooper.test

import androidx.room.*

@Dao
interface ProfileDao {
    @Query("SELECT * FROM Profile")
    fun getAll(): List<Profile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProfile(profile : Profile)

    @Query("SELECT * FROM Profile WHERE profileName=:profile")
    fun readProfile(profile: String) : Profile

    @Delete
    fun deleteProfile(profile: Profile)
}