package com.example.whatsup.model

data class User (val name : String, val bio : String, val profilePicturePath : String?) {

    constructor() : this("", "", null)

}