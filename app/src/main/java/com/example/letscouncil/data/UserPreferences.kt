package com.example.letscouncil.data

import android.content.Context
import android.content.SharedPreferences
import com.example.letscouncil.data.model.User

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        sharedPreferences.edit().apply {
            putString("name", user.name)
            putInt("birthYear", user.birthYear)
            putInt("birthMonth", user.birthMonth)
            putInt("birthDay", user.birthDay)
            putString("occupation", user.occupation)
            apply()
        }
    }

    fun getUser(): User? {
        val name = sharedPreferences.getString("name", null)
        val birthYear = sharedPreferences.getInt("birthYear", -1)
        val birthMonth = sharedPreferences.getInt("birthMonth", -1)
        val birthDay = sharedPreferences.getInt("birthDay", -1)
        val occupation = sharedPreferences.getString("occupation", null)

        return if (name != null && birthYear != -1 && birthMonth != -1 && birthDay != -1 && occupation != null) {
            User(name, birthYear, birthMonth, birthDay, occupation)
        } else {
            null
        }
    }
}
