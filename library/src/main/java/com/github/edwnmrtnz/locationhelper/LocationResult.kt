package com.github.edwnmrtnz.locationhelper

import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import java.lang.Exception

sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    data class Failed(val raw: Exception?) : LocationResult()
    object NoPermission : LocationResult()
    object GpsNotOpen : LocationResult()

    //Device have issues that cant be resolve. Maybe no gps?
    object NotResolvable : LocationResult()

    /*
     // Location settings are not satisfied. But could be fixed by showing the user a dialog.
        try {
            // Cast to a resolvable exception.
            ResolvableApiException resolvable = (ResolvableApiException) exception;
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            resolvable.startResolutionForResult(
                OuterClass.this,
                REQUEST_CHECK_SETTINGS);
        } catch (SendIntentException e) {
            // Ignore the error.
        }
     */
    class Resolvable(val exception : ResolvableApiException) : LocationResult()
}