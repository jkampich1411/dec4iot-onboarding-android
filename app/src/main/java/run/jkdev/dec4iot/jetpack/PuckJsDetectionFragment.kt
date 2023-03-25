package run.jkdev.dec4iot.jetpack

import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import run.jkdev.dec4iot.jetpack.gsonmodels.DeviceDetectionQr
import java.lang.Byte

/**
 * A simple [Fragment] subclass.
 * Use the [PuckJsDetectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PuckJsDetectionFragment : Fragment() {
    private val args: PuckJsDetectionFragmentArgs by navArgs()
    private val nfcViewModel = ViewModelProvider(publicMainActivityThis)[NfcDataViewModel::class.java]

    private var sensorId: String = ""
    private var endpoint: String = ""
    private var pView: View? = null

    private var readCmd: ByteArray = byteArrayOf(0x30)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_puck_js_detection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.sensorId = args.sensorId
        this.endpoint = args.endpoint

        this.pView = view

        nfcViewModel.shouldBeListening.value = true

        nfcViewModel.nfcData.observeForever { this@PuckJsDetectionFragment.asyncNfcConnection(it) }
    }

    private fun asyncNfcConnection(tag: Tag) {
        val thread = Thread {
            nfcViewModel.shouldBeListening.postValue(false)

            val ndef = Ndef.get(tag)

            try {
                ndef.connect()
                Log.i(TAG, "the what and who now? ")
                Log.i(TAG, ndef.toString())

                val records = ndef.ndefMessage.records
                val record = records[0]
                Log.i(TAG, record.toString())

            } catch (ex: android.nfc.TagLostException) {
                nfcViewModel.shouldBeListening.postValue(true)
            } catch (ex: java.io.IOException) {
                nfcViewModel.shouldBeListening.postValue(true)
                Log.e(TAG, ex.stackTraceToString())
            } catch (ex: Throwable) {
                Log.e(TAG, ex.stackTraceToString())
            }
        }
        thread.start()
    }

    private fun noThisWontWork() {
        this.pView?.findViewById<TextView>(R.id.resultsText)
            ?.text = getString(R.string.no_dec4iot_devicedetection)

        this.pView?.findViewById<Button>(R.id.continueButton)
            ?.text = "Try again"

        this.pView?.findViewById<Button>(R.id.continueButton)
            ?.setOnClickListener {
                val act = PuckJsDetectionFragmentDirections.actionPuckJsFragmentToQrScanFragment()
                findNavController().navigate(act)
            }
    }

    fun enableAndSetData(data: DeviceDetectionQr) {
        this.pView?.findViewById<TextView>(R.id.pleaseTouchText)
            ?.visibility = View.INVISIBLE

        this.pView?.findViewById<ImageView>(R.id.nfcImage)
            ?.visibility = View.INVISIBLE

        if(data.trustyDevice !== "I am your trusty Dec4IoT Device") { return noThisWontWork() }
    }

    override fun onDestroy() {
        super.onDestroy()

        nfcViewModel.shouldBeListening.value = false
    }

}