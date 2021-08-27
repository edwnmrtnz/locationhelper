package com.github.edwnmrtnz.locationhelper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

internal fun Context.isPermissionEnabled(): Boolean {
    val isFineLocGranted = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val isCoarseLocGranted = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return isFineLocGranted && isCoarseLocGranted
}