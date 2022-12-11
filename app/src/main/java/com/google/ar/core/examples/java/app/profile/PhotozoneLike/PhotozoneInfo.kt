package com.google.ar.core.examples.java.app.profile.PhotozoneLike

data class PhotozoneInfo(
    val altitude : Double? = 0.0,
    val latitude : Double? = 0.0,
    val longitude : Double? = 0.0,
    val imgURL : String? = "",
    val photozoneName : String? = "",
    val photozoneLikes : Long? = 0L
)
