package com.google.ar.core.examples.java.app.board

import java.io.Serializable

data class PhotozoneData(
    val imgURL : String = "",
    val documentId : String = "",
    val latitude : Number? = 0.0,

    val longitude : Number?= 0.0,
    val altitude : Number? = 0.0,
    val heading : Number? = 0.0,
    val placeCluster: String = ""
) : Serializable
