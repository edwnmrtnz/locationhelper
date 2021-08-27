package com.github.edwnmrtnz.locationhelper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.resume

class LocationHelper(private val applicationContext: Context) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }
    private val settingsClient: SettingsClient by lazy {
        LocationServices.getSettingsClient(applicationContext)
    }

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create()
            .setInterval(3000)
            .setFastestInterval(1000)
            .setPriority(PRIORITY_HIGH_ACCURACY)
    }

    private val locationSettingsRequest: LocationSettingsRequest by lazy {
        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
    }

    fun isPermissionEnabled(): Boolean {
        return applicationContext.isPermissionEnabled()
    }

    private fun isGpsOpen(): Boolean {
        val manager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getViableCurrentLocation(accuracy: Float = 100f): LocationResult {
        val audit = audit()
        if (audit is AuditResult.Failed)
            return audit.result

        return suspendCancellableCoroutine { continuation ->
            val callback = object : LocationCallback() {
                override fun onLocationResult(p0: com.google.android.gms.location.LocationResult) {
                    if (p0.locations.isNotEmpty()) {
                        val loc = p0.locations.first()
                        Log.e("Hello", "Found location: $loc")
                        if(loc.accuracy <= accuracy) {
                            continuation.resume(LocationResult.Success(p0.locations.first()))
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        }
    }

    // Returns a single current location fix on the device. Unlike getLastLocation() that
    // returns a cached location, this method could cause active location computation on the
    // device. A single fresh location will be returned if the device location can be
    // determined within reasonable time (tens of seconds), otherwise null will be returned.
    suspend fun getFixCurrentLocation(): LocationResult {
        return getOneShotLocation(PRIORITY_HIGH_ACCURACY)
    }

    fun getLocations() : Flow<LocationResult> {
        TODO()
    }

    @SuppressLint("MissingPermission")
    private suspend fun getOneShotLocation(priority: Int): LocationResult {
        val audit = audit()
        if (audit is AuditResult.Failed)
            return audit.result

        val cancellationTokenSource = CancellationTokenSource()
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient
                .getCurrentLocation(priority, cancellationTokenSource.token)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val result: Location = task.result
                        continuation.resume(LocationResult.Success(result))
                    } else {
                        val exception = task.exception
                        continuation.resume(LocationResult.Failed(exception))
                    }
                }
            continuation.invokeOnCancellation {
                Log.e("LocationHelper", "Viable Location Task Cancelled")
                cancellationTokenSource.cancel()
            }
        }
    }

    private suspend fun audit(): AuditResult {
        if (!isPermissionEnabled()) {
            return AuditResult.Failed(LocationResult.NoPermission)
        }
        if (!isGpsOpen()) {
            return AuditResult.Failed(LocationResult.GpsNotOpen)
        }

        return suspendCancellableCoroutine { continuation ->
            settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnCompleteListener { task ->
                    try {
                        task.getResult(ApiException::class.java)
                        continuation.resume(AuditResult.Success)
                    } catch (exception: ApiException) {
                        when (exception.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                val resolvableException = exception as ResolvableApiException
                                continuation.resume(
                                    AuditResult.Failed(
                                        LocationResult.Resolvable(
                                            resolvableException
                                        )
                                    )
                                )
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                continuation.resume(AuditResult.Failed(LocationResult.NotResolvable))
                            }
                            else -> continuation.resume(
                                AuditResult.Failed(
                                    LocationResult.Failed(
                                        exception
                                    )
                                )
                            )
                        }
                    }
                }
        }
    }

    private sealed class AuditResult {
        object Success : AuditResult()
        class Failed(val result: LocationResult) : AuditResult()
    }

    companion object {
        fun getRequiredPermissions(): Array<String> {
            return arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
    }
}
