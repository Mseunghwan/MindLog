package com.min.mindlog.data.model

data class User(
    val name: String,
    val birthYear: Int,
    val birthMonth: Int,
    val birthDay: Int,
    val occupation: String,
    var score: Int,
)
