package com.example.ble.ui.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

class LocalScanCallback(
    private val callback: (BluetoothDevice) -> Unit
) : ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        addScanResult(result)
    }

    override fun onBatchScanResults(results: List<ScanResult?>) {
        results.filterNotNull().forEach {
            addScanResult(it)
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Log.e("BLE_SCANNING", "BLE Scan Failed with code $errorCode")
    }

    private fun addScanResult(result: ScanResult) {
        callback(result.device)
    }
}