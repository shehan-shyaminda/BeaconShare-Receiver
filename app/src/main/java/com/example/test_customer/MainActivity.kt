package com.example.test_customer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.test_customer.databinding.ActivityMainBinding

class MainActivity : ComponentActivity(), BeaconScanListener {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var scanner: BeaconScanner
    private lateinit var loader: LoadingDialog

    private val requestBluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isBluetoothPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions[Manifest.permission.BLUETOOTH_SCAN] == true && permissions[Manifest.permission.BLUETOOTH_CONNECT] == true && permissions[Manifest.permission.BLUETOOTH_ADMIN] == true && permissions[Manifest.permission.BLUETOOTH_ADVERTISE] == true
            } else {
                permissions[Manifest.permission.BLUETOOTH] == true && permissions[Manifest.permission.BLUETOOTH_ADMIN] == true
            }

            if (!isBluetoothPermissionGranted) {
                Log.d(TAG, "Please Enable Bluetooth Permission in Settings")
            } else {
                checkAndProceedWithLocationPermission()
            }
        }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isLocationPermissionGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (!isLocationPermissionGranted) {
                Log.d(TAG, "Please Enable Location Permission in Settings")
            } else {
                visibleForMerchants()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loader = LoadingDialog(this)
        scanner = BeaconScanner(this, loader, this)

        binding.btnReceiver.setOnClickListener {
            if (!checkBluetoothPermissions(this@MainActivity)) return@setOnClickListener
            if (!checkLocationPermissions(this@MainActivity)) return@setOnClickListener

            visibleForMerchants()
        }
    }
    override fun onScanCompleted(merchantDetails: String?) {
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra("merchantDetails", merchantDetails ?: "No merchant detected")
        startActivity(intent)
    }

    private fun checkAndProceedWithLocationPermission() {
        if (!checkLocationPermissions(this@MainActivity)) {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            visibleForMerchants()
        }
    }

    private fun visibleForMerchants() {
        Log.d(TAG, "visibleForMerchants: Merchant is visible")
        scanner.startScan()
    }

    private fun checkBluetoothPermissions(activity: Activity): Boolean {
        val bluetoothPermissionsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (!bluetoothPermissionsGranted) {
            requestBluetoothPermissionLauncher.launch(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    )
                } else {
                    arrayOf(
                        Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN
                    )
                }
            )
        }
        return bluetoothPermissionsGranted
    }

    private fun checkLocationPermissions(activity: Activity): Boolean {
        val locationPermissionsGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!locationPermissionsGranted) {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        return locationPermissionsGranted
    }
}