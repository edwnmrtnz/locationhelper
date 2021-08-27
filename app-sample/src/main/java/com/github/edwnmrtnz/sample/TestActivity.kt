package com.github.edwnmrtnz.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.github.edwnmrtnz.locationhelper.LocationHelper
import com.github.edwnmrtnz.locationhelper.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity() {
    private val helper: LocationHelper by lazy {
        LocationHelper(this.applicationContext)
    }

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val checkPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var isGranted = true
        permissions.entries.forEach {
            Log.e("Hello", "${it.key} -> ${it.value}")
            if (!it.value)
                isGranted = false
        }
        if (isGranted)
            requestLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }  override fun onStart() {
        super.onStart()
        if (helper.isPermissionEnabled()) {
            requestLocation()
        } else {
            checkPermissions.launch(LocationHelper.getRequiredPermissions())
        }
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
    }

    private fun requestLocation() {
        setResult("Start request...")
        job = scope.launch {
            when (val result = helper.getViableCurrentLocation(100f)) {
                is LocationResult.Success -> {
                    setResult("Success:\n" + result.location.toString())
                }
                is LocationResult.Failed -> {
                    setResult("Failed:\n" + result.raw.toString())
                }
                LocationResult.GpsNotOpen -> setResult("Gps is not open")
                LocationResult.NoPermission -> setResult("No Permission")
                LocationResult.NotResolvable -> setResult("Not Resolvable")
                is LocationResult.Resolvable -> setResult("Resolvable: ${result.exception}")
            }
        }
    }

    private fun setResult(text: String) {
        findViewById<TextView>(R.id.tvHello).text = text
    }
}