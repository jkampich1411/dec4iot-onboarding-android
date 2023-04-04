package run.jkdev.dec4iot.jetpack

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import run.jkdev.dec4iot.jetpack.ble.BleAdapter
import run.jkdev.dec4iot.jetpack.ble.Espruino
import run.jkdev.dec4iot.jetpack.interfaces.NordicUUIDs


/**
 * A simple [Fragment] subclass.
 * Use the [PuckJsDetectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PuckJsDetectionFragment : Fragment() {
    private val args: PuckJsDetectionFragmentArgs by navArgs()

    private var sensorId: String = ""
    private var endpoint: String = ""
    private var pView: View? = null

    private val espruino = Espruino()
    private val le = BleAdapter()

    private val foundDevicesList = mutableSetOf<BluetoothDevice>()
    private val foundDevicesLiveData = MutableLiveData<MutableSet<BluetoothDevice>>()

    private val deviceButtonMutableMap = mutableMapOf<Button, BluetoothDevice>()
    private val deviceServiceMutableMap = mutableMapOf<BluetoothGatt, BluetoothGattService?>()
    private val deviceServicesDiscoveredLiveDataMutableMap = mutableMapOf<BluetoothGatt, MutableLiveData<Boolean>>()
    private val deviceConnections = mutableSetOf<BluetoothGatt?>()

    private val currentSelection = MutableLiveData<BluetoothGatt>()
    private var lastSelection: BluetoothGatt? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_puck_js_detection, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.sensorId = args.sensorId
        this.endpoint = args.endpoint

        this.pView = view

        espruino.addCallback(this.scanCallback)
        espruino.startScanning()

        foundDevicesLiveData.observeForever { set ->
            val layout = view.findViewById<LinearLayout>(R.id.foundDevicesButtons)

            set.forEach {
                if(layout.findViewById<Button>(it.hashCode()) == null) {
                    val btn = createNewDeviceButton(it.name, deviceButtonOnClickListener, it.hashCode())

                    deviceButtonMutableMap[btn] = it
                    layout.addView(btn)
                }
            }
        }

        currentSelection.observeForever {
            if(deviceServicesDiscoveredLiveDataMutableMap[lastSelection]!!.value == true) {
                val service = deviceServiceMutableMap[lastSelection]
                val chara = service!!.getCharacteristic(NordicUUIDs.TX_CHARACTERISTIC)
                lastSelection!!.writeCharacteristic(chara, espruino.discoveredCmd, WRITE_TYPE_NO_RESPONSE)
                lastSelection!!.disconnect()
            }
        }

        deviceServicesDiscoveredLiveDataMutableMap.forEach { (t, u) ->
            u.observeForever {
                if(it == true) {
                    servicesDiscoveredSendCommands(t, deviceServiceMutableMap[t]!!)
                }
            }
        }
    }

    private fun servicesDiscoveredSendCommands(gatt: BluetoothGatt, service: BluetoothGattService) {
        val charac = service.getCharacteristic(NordicUUIDs.TX_CHARACTERISTIC)
        gatt.writeCharacteristic(charac, espruino.discoveryCmd, WRITE_TYPE_NO_RESPONSE)

    }

    private fun createNewDeviceButton(text: String, onClickListener: OnClickListener, id: Int): Button {
        val btn = Button(publicApplicationContext)

        btn.setOnClickListener(onClickListener)
        btn.text = text
        btn.id = id

        return btn
    }

    @SuppressLint("MissingPermission")
    private val deviceButtonOnClickListener = OnClickListener {
        val btn = it.findViewById<Button>(it.id)

        val device = deviceButtonMutableMap[btn]
        if(!isDeviceConnected(device!!.address)) {
            val connection = device.connectGatt(publicApplicationContext, false, gattConnectionCallback)

            deviceConnections.add(connection)
            lastSelection = currentSelection.value
            currentSelection.postValue(connection)
        }
    }

    private fun deviceServicesDiscovered(connection: BluetoothGatt, discovered: Boolean) {
        deviceServicesDiscoveredLiveDataMutableMap.forEach { (t, u) ->
            if(connection.hashCode() == t.hashCode()) {
                u.postValue(discovered)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val gattConnectionCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if(newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
            }
            if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                deviceServiceMutableMap[gatt!!] = null
                deviceConnections.remove(gatt)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            val service = gatt!!.getService(NordicUUIDs.SERVICE)
            deviceServiceMutableMap[gatt] = service
            deviceServicesDiscovered(gatt, true)
        }

    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            try {
                if(result!!.device !in foundDevicesList) {
                    foundDevicesList.add(result.device)
                    foundDevicesLiveData.postValue(foundDevicesList)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during LeScanCallback", e)
            }

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "BLE scanning failed / No device found. Error Code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    private fun isDeviceConnected(deviceAddress: String): Boolean {
        var connected = false

        // Check if the device is currently connected
        val connectedDevices = le.bluetoothManager!!.getConnectedDevices(BluetoothProfile.GATT)
        for (device in connectedDevices) {
            connected = device.address == deviceAddress
        }

        // Device is not connected
        return connected
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()

        espruino.stopScanning()
        deviceConnections.forEach { it!!.disconnect() }
    }

    override fun onResume() {
        super.onResume()

        espruino.startScanning()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        espruino.stopScanning()
        deviceConnections.forEach { it!!.disconnect() }
    }

}