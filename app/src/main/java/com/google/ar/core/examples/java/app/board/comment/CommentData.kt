package com.google.ar.core.examples.java.app.board.comment

import java.io.Serializable

data class CommentData(
    var comment : String,
    val uid : String,
    var posted_time : String
) : Serializable
