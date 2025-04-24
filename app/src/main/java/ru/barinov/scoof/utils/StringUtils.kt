package ru.barinov.scoof.utils

import java.util.regex.Pattern

object StringUtils {
    
    /**
     * Делает первую букву строки заглавной
     */
    fun capitalize(str: String): String {
        if (str.isEmpty()) return str
        return str.substring(0, 1).uppercase() + str.substring(1)
    }
    
    /**
     * Проверяет валидность email
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        val pattern = Pattern.compile(emailRegex)
        return pattern.matcher(email).matches()
    }
    
    /**
     * Переворачивает строку
     */
    fun reverse(str: String): String {
        return str.reversed()
    }
} 