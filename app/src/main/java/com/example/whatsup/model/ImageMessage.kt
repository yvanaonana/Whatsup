package com.example.whatsup.model

import java.util.*

data class ImageMessage(val ImagePath: String,
                       override val time: Date,
                       override val senderId: String,
                       override val type: String = MessageType.IMAGE)
    : Message {
    constructor() : this("t", Date(0), "")
}