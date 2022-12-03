package com.google.ar.core.examples.java.app.profile

interface PermissionListener {
    fun onPermissionGranted()

    fun onPermissionDenied(deniedPermissions: ArrayList<String?>?)
}
