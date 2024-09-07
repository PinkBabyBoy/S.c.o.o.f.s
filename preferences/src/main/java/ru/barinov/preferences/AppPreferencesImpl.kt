package ru.barinov.preferences

import android.content.SharedPreferences

internal class AppPreferencesImpl(sharedPreferences: SharedPreferences): AppPreferences{

    private companion object{
       private const val T_PASS_KEY = "tPass"
       private const val F_PASS_KEY = "fPass"
       private const val IV_KEY = "iv"
    }

    override var tPass by sharedPreferences.string(T_PASS_KEY, null)
    override var fPass by sharedPreferences.string(F_PASS_KEY, null)
    override var iv: String? by sharedPreferences.string(IV_KEY, null)
}
