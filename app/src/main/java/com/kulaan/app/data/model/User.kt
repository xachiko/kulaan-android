package com.kulaan.app.data.model

data class User(
    val id_user: Int,
    val name: String,
    val email: String,
    val roles: List<String>
)
