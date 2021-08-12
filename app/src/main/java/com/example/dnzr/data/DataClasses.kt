package com.example.dnzfind.data

data class DanceGroups(val groupAlias : String, val groupName : String ,val members : List<String> )

data class Style(val id: Integer, val desc: String )

data class Dnzr (val userName: String, val role : String, val styles : List<String>, val status : String)