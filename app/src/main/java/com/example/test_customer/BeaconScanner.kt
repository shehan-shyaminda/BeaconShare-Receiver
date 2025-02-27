package com.example.test_customer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.UUID

class BeaconScanner(
    private val context: Context,
    private val loader: LoadingDialog,
    private val listener: BeaconScanListener
) {
    private var bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner
    private val merchantId = "4000"

    fun startScan() {

        if (scanner == null) {
            println("BLE Scanning not supported")
            return
        }

        val filter =
            ScanFilter.Builder().setServiceUuid(ParcelUuid(generateFixedUUID(merchantId))).build()

        val settings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("TAG", "startScan: Permissions Denied")
            return
        }
        loader.show()
        scanner.startScan(listOf(filter), settings, scanCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(scanCallback)
            Log.d("BluetoothScan", "Scan stopped automatically after timeout")
        }, 60000L)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            loader.dismiss()
            val data = result.scanRecord?.getServiceData(ParcelUuid(generateFixedUUID(merchantId)))
            if (data != null) {
                val merchantDetails = String(data)
                Log.d("TAG", "Detected Merchant: $merchantDetails")
                stopScanning(context)
                listener.onScanCompleted(merchantDetails)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            loader.dismiss()
            stopScanning(context)
            Log.d("TAG", "Beacon scan failed: $errorCode")
            listener.onScanCompleted(null)  // Notify listener of failure
        }
    }

    fun stopScanning(context: Context) {
        if (scanner != null) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            scanner.stopScan(scanCallback)
        }
    }


    private fun generateFixedUUID(value: String): UUID {
        val truncatedId = value.hashCode() and 0xFFFF
        return UUID.fromString(String.format("0000%04X-0000-1000-8000-00805F9B34FB", truncatedId))
    }
}