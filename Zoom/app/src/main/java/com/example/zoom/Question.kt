package com.example.zoom

data class Question(
    var content:String="",
    var comment_count:Int,
    var comment:String="",
    var isExpanded : Boolean=false
)
