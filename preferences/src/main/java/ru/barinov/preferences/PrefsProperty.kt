package ru.barinov.preferences

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class PrefsProperty<T>(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val default: T,
    private val getter: SharedPreferences.(String, T) -> T,
    private val setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
) : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return  sharedPreferences.getter(key, default)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        sharedPreferences.edit().setter(key, value).apply()
    }
}

internal fun SharedPreferences.string(key: String, def: String?) = PrefsProperty(
    this,
    key,
    def,
    SharedPreferences::getString,
    SharedPreferences.Editor::putString
)

internal fun SharedPreferences.stringSet(key: String, def: Set<String>) = PrefsProperty(
    this,
    key,
    def,
    SharedPreferences::getStringSet,
    SharedPreferences.Editor::putStringSet
)

internal fun SharedPreferences.stringNotNull(key: String, def: String) = PrefsProperty(
    this,
    key,
    def,
    SharedPreferences::stringNotNullGetter,
    SharedPreferences.Editor::putString
)

private fun SharedPreferences.stringNotNullGetter(key: String, def: String) =
     getString(key, def) ?: def

internal fun SharedPreferences.float(key: String, def: Float) = PrefsProperty(
    this,
    key,
    def,
    SharedPreferences::getFloat,
    SharedPreferences.Editor::putFloat
)

internal fun SharedPreferences.int(key: String, def: Int) = PrefsProperty(
    this,
    key,
    def,
    SharedPreferences::getInt,
    SharedPreferences.Editor::putInt
)

internal fun SharedPreferences.long(key: String, def: Long) = PrefsProperty(
    this,
    key,
    def,
    SharedPreferences::getLong,
    SharedPreferences.Editor::putLong
)

internal fun SharedPreferences.boolean(key: String, def: Boolean) = PrefsProperty(
    this,
    key,
    def,
    SharedPreferences::getBoolean,
    SharedPreferences.Editor::putBoolean
)
