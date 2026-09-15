package com.example.shifttrack.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class LocationResult {
    data class Success(val latitude: Double, val longitude: Double, val accuracy: Float) : LocationResult()
    object GpsDisabled : LocationResult()
    object NoPermission : LocationResult()
    data class Error(val message: String) : LocationResult()
}

class LocationClient(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isGpsEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
               lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        if (!isGpsEnabled()) {
            return LocationResult.GpsDisabled
        }

        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        if (continuation.isActive) {
                            continuation.resume(
                                LocationResult.Success(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    accuracy = location.accuracy
                                )
                            )
                        }
                    } else {
                        // Fallback to last known location
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (!continuation.isActive) return@addOnSuccessListener
                            if (lastLoc != null) {
                                continuation.resume(
                                    LocationResult.Success(
                                        latitude = lastLoc.latitude,
                                        longitude = lastLoc.longitude,
                                        accuracy = lastLoc.accuracy
                                    )
                                )
                            } else {
                                continuation.resume(
                                    LocationResult.Error("Unable to acquire GPS fix. Please ensure location services are enabled.")
                                )
                            }
                        }.addOnFailureListener {
                            if (continuation.isActive) {
                                continuation.resume(LocationResult.Error(it.message ?: "Failed to acquire location"))
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(LocationResult.Error(it.message ?: "Location request failed"))
                    }
                }

            continuation.invokeOnCancellation {
                cts.cancel()
            }
        }
    }
}
