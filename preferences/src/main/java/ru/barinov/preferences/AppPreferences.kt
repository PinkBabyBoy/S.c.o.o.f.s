package ru.barinov.preferences


interface AppPreferences {
    var tPass: String?
    var fPass: String?
    var iv: String?
    var shownOnBoardings: Set<String>?
    var workId: String?
}