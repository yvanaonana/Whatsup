package com.example.whatsup.model

data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}