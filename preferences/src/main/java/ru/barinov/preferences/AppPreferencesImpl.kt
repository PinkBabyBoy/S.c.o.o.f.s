package ru.barinov.preferences

import android.content.SharedPreferences

internal class AppPreferencesImpl(sharedPreferences: SharedPreferences): AppPreferences{

    private companion object{
       const val T_PASS_KEY = "tPass"
       const val F_PASS_KEY = "fPass"
       const val IV_KEY = "iv"
       const val SHOWED_ONBOARDINGS_KEY = "onboardings"
    }

    override var tPass by sharedPreferences.string(T_PASS_KEY, null)
    override var fPass by sharedPreferences.string(F_PASS_KEY, null)
    override var iv: String? by sharedPreferences.string(IV_KEY, null)
    override var shownOnBoardings: Set<String>? by sharedPreferences.stringSet(SHOWED_ONBOARDINGS_KEY, setOf())
}
