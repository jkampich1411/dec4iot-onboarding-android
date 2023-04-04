package run.jkdev.dec4iot.jetpack

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import run.jkdev.dec4iot.jetpack.ble.BleAdapter
import run.jkdev.dec4iot.jetpack.ble.Espruino

class PuckJsWritingFragment : Fragment() {
    private val args: PuckJsWritingFragmentArgs by navArgs()

    private var sensorId: String = ""
    private var endpoint: String = ""
    private var puckJsMac: String = ""

    private val espruino = Espruino()
    private val le = BleAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_puck_js_writing, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorId = args.sensorId
        endpoint = args.endpoint
        puckJsMac = args.puckJsMac

        espruino.startScanning(puckJsMac)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            // Device found, continue

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            Log.w(TAG, "No device found!")
        }
    }

    override fun onPause() {
        super.onPause()

        espruino.stopScanning()
    }

    override fun onResume() {
        super.onResume()

        espruino.startScanning(puckJsMac)
    }

    override fun onDestroy() {
        super.onDestroy()

        espruino.stopScanning()
    }

}