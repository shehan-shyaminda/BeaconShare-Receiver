Code Snipit for Beacon Share

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
