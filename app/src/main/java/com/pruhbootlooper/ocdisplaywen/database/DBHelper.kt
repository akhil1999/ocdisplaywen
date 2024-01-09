package com.pruhbootlooper.ocdisplaywen.database

import android.content.Context
import androidx.room.Room
import com.pruhbootlooper.test.AppDatabase
import com.pruhbootlooper.test.Profile
import kotlin.concurrent.thread

class DBHelper {
    companion object{
        private lateinit var db : AppDatabase

        /*
            function: initialises DB

            accepts:
            1. context

            returns:
            Nothing
        */
        fun initDB(context: Context){
            thread{
                db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "Profile"
                ).build()

                //60Hz profile
                DBHelper.setProfileInDB("stock_profile", 3, 127, 0)
                //82Hz profile
                DBHelper.setProfileInDB("oc_profile", 3, 173, 0)
                //current
                DBHelper.setProfileInDB("current_profile", 3, 127, 0)
            }
        }

        /*
            function: sets a profile in DB

            accepts:
            1. profile : String
            2. Pdiv value : Int
            3. Mdiv value : Int
            4. Sdiv value : Int

            returns:
            Nothing
        */
        fun setProfileInDB(profile : String, P : Int, M : Int, S : Int){
            thread{
                val profileDao = db.profileDao()
                val profile = Profile(profile, P, M, S)
                profileDao.insertProfile(profile)
            }
        }

        /*
            function: initialises DB

            accepts:
            1.profile_name

            returns:
            Profile (contains profile_name, Pdiv, Mdiv, Sdiv)
        */
        fun getProfileFromDB(profile : String, response : (Profile) -> Unit) {
            thread{
                val profileDao = db.profileDao()
                val profile = profileDao.readProfile(profile)
                response(profile)
            }
        }

        /*
            function: deletes profile in DB

            accepts:
            1.profile_name

            returns:
            Nothing
        */
        fun deleteProfileFromDB(profile : Profile){
            thread{
                val profileDao = db.profileDao()
                profileDao.deleteProfile(profile)
            }
        }
    }
}