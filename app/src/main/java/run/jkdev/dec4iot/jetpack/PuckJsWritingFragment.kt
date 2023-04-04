package run.jkdev.dec4iot.jetpack

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.VibrationEffect
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import run.jkdev.dec4iot.jetpack.ble.BleAdapter
import run.jkdev.dec4iot.jetpack.ble.Espruino
import run.jkdev.dec4iot.jetpack.interfaces.NordicUUIDs

class PuckJsWritingFragment : Fragment() {
    private val args: PuckJsWritingFragmentArgs by navArgs()

    private var sensorId: String = ""
    private var endpoint: String = ""
    private var puckJsMac: String = ""
    private var puckJsName: String = ""

    private val espruino = Espruino()
    private val le = BleAdapter()

    private var pView: View? = null

    private var infoTitle: TextView? = null
    private var sensorIdText: TextView? = null
    private var apiEndpointText: TextView? = null
    private var checkedAllCheckBox: CheckBox? = null
    private var restartButton: Button? = null
    private var continueButton: Button? = null

    private val leDevice = MutableLiveData<BluetoothDevice>()
    private var leGatt: BluetoothGatt? = null
    private var leService: BluetoothGattService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_puck_js_writing, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorId = args.sensorId
        endpoint = args.endpoint
        puckJsMac = args.puckJsMac
        puckJsName = args.puckJsName

        pView = view

        infoTitle = view.findViewById(R.id.writingInfo_puckJs)
        sensorIdText = view.findViewById(R.id.sensorIdToBeWritten_puckJs)
        apiEndpointText = view.findViewById(R.id.apiEndpointToBeWritten_puckJs)
        checkedAllCheckBox = view.findViewById(R.id.valuesChecked_puckJs)
        restartButton = view.findViewById(R.id.restart_puckJs)
        continueButton = view.findViewById(R.id.write_puckJs)

        infoTitle!!.text =
            getString(R.string.the_following_will_be_written, puckJsName)

        sensorIdText!!.text =
            getString(R.string.sensor_id_to_be_written, sensorId)

        apiEndpointText!!.text =
            getString(R.string.api_endpoint_to_be_written, endpoint)

        restartButton!!.setOnClickListener(restartBtnListener)

        continueButton!!.setOnClickListener(continueBtnListener)

        espruino.addCallback(scanCallback)
        espruino.startScanning(puckJsMac)

        leDevice.observeForever {
            it.connectGatt(publicApplicationContext, false, gattCallback)
        }
    }

    @SuppressLint("MissingPermission")
    private val continueBtnListener = OnClickListener {
        val isChecked = checkedAllCheckBox!!.isChecked

        if(isChecked && leGatt != null && leService != null && le.isDeviceConnected(leGatt!!.device.address)) { // Point of no return reached!
            val chara = leService!!.getCharacteristic(NordicUUIDs.TX_CHARACTERISTIC)
            val valToSend = espruino.writeConfigCmdPuckJs("main.json", sensorId, endpoint, "1")

            leGatt!!.writeCharacteristic(chara, valToSend, WRITE_TYPE_NO_RESPONSE)

            leGatt!!.disconnect()

            val act = PuckJsWritingFragmentDirections.actionPuckJsWritingFragmentToOnboardingDone()
            findNavController().navigate(act)
        } else {
            Toast.makeText(publicApplicationContext, R.string.please_confirm_all_data, Toast.LENGTH_LONG).show()
            publicVibrator.vibrate(VibrationEffect.createOneShot(1000, 100))
        }
    }

    private val restartBtnListener = OnClickListener {
        val act = PuckJsWritingFragmentDirections.actionPuckJsWritingFragmentToQrScanFragment()
        findNavController().navigate(act)
    }

    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if(newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
                leGatt = gatt
            }
            if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt?.connect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            val service = gatt?.getService(NordicUUIDs.SERVICE)
            leService = service
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            leDevice.postValue(result!!.device)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.w(TAG, "No device found!")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()

        espruino.stopScanning()
        leGatt?.disconnect()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        espruino.startScanning(puckJsMac)
        leGatt?.connect()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        espruino.stopScanning()
        leGatt?.disconnect()
    }

}