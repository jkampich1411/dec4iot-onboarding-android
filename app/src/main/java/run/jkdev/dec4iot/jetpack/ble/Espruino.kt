package run.jkdev.dec4iot.jetpack.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.os.VibrationEffect
import android.util.Log
import android.widget.Toast
import run.jkdev.dec4iot.jetpack.*
import run.jkdev.dec4iot.jetpack.interfaces.NordicUUIDs
import java.util.*

class Espruino {
    val discoveryCmd = "discovery()\n".toByteArray(Charsets.UTF_8)
    val discoveredCmd = "discovered()\n".toByteArray(Charsets.UTF_8)
    fun writeConfigCmd(fileName: String, id: String, endpoint: String): ByteArray {
        return "writeConfig('$fileName','$id','$endpoint')\n".toByteArray(Charsets.UTF_8)
    }


    private val leScanner = BleAdapter().bluetoothAdapter.bluetoothLeScanner
    private var scanning = false

    private val scanCallbacks = mutableListOf<ScanCallback>()

    private val scanSettings = ScanSettings.Builder()
        .build()

    private val scanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(NordicUUIDs.SERVICE))
        .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            scanCallbacks.forEach { it.onScanResult(callbackType, result) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scanCallbacks.forEach { it.onScanFailed(errorCode) }
        }
    }

    fun addCallback(scanCallback: ScanCallback) {
        scanCallbacks.add(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (!allPermGranted()) {
            Toast.makeText(publicApplicationContext, R.string.permission_not_granted_no_continue, Toast.LENGTH_LONG).show()
            publicVibrator.vibrate(VibrationEffect.createOneShot(1000, 100))
            return
        }

        leScanner.startScan(listOf<ScanFilter>(scanFilter), scanSettings, scanCallback)
        scanning = true
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!allPermGranted()) {
            Log.e(TAG, "Something terrible went wrong. I truly have no Idea why!")
            return
        }
        leScanner.stopScan(scanCallback)
        scanning = false

    }


}