package run.jkdev.dec4iot.jetpack

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import run.jkdev.dec4iot.jetpack.gsonmodels.BangleJsConfig

class BangleJsNotLikeThisFragment : Fragment() {
    val args: BangleJsNotLikeThisFragmentArgs by navArgs()

    private var fromIntent: Boolean? = null
    private var sensorIdValue: String? = null
    private var apiEndpointValue: String? = null

    private val gson = Gson()

    private var funView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bangle_js_not_here, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        funView = view

        fromIntent = args.fromIntent
        sensorIdValue = args.sensorId
        apiEndpointValue = args.endpoint

        val writingInfo: TextView = view.findViewById(R.id.writingInfo_banglejs)
        val sensorId: TextView = view.findViewById(R.id.sensorIdToBeWritten_banglejs)
        val apiEndpoint: TextView = view.findViewById(R.id.apiEndpointToBeWritten_banglejs)
        val pleaseConfirm: TextView = view.findViewById(R.id.pleaseConfirm_banglejs)
        val confirmed: CheckBox = view.findViewById(R.id.valuesChecked_banglejs)
        val restart: Button = view.findViewById(R.id.restart_banglejs)
        val confirm: Button = view.findViewById(R.id.write_banglejs)

        if(fromIntent == true) {
            writingInfo.visibility = View.VISIBLE
            writingInfo.text =
                getString(R.string.the_following_will_be_written, "your Bangle.JS")

            sensorId.visibility = View.VISIBLE
            sensorId.text =
                getString(R.string.sensor_id_to_be_written, sensorIdValue)

            apiEndpoint.visibility = View.VISIBLE
            apiEndpoint.text =
                getString(R.string.api_endpoint_to_be_written, apiEndpointValue)

            pleaseConfirm.visibility = View.VISIBLE

            confirmed.visibility = View.VISIBLE

            restart.visibility = View.VISIBLE
            restart.setOnClickListener(restartBtnListener)

            confirm.visibility = View.VISIBLE
            confirm.setOnClickListener(continueBtnListener)
        }

    }

    private val continueBtnListener = OnClickListener {
        val config = BangleJsConfig(true, sensorIdValue!!.toInt(), apiEndpointValue!!, 60)
        val configJson = gson.toJson(config)

        val confirmed: CheckBox = funView!!.findViewById(R.id.valuesChecked_banglejs)
        if(!confirmed.isChecked) {
            Toast.makeText(publicApplicationContext, R.string.please_confirm_all_data, Toast.LENGTH_LONG).show()
            publicVibrator.vibrate(VibrationEffect.createOneShot(1000, 100))
        } else {
            val writeIntent = Intent("com.banglejs.uart.tx")
            writeIntent.putExtra("line", "require('Storage').writeJSON('dec4iot.settings.json', $configJson)")

            val feedbackIntent = Intent("com.banglejs.uart.tx")
            feedbackIntent.putExtra("line", "Bangle.buzz(1000, 1)")

            publicApplicationContext.sendBroadcast(writeIntent)
            publicApplicationContext.sendBroadcast(feedbackIntent)

            val act = BangleJsNotLikeThisFragmentDirections.actionBangleJsNotLikeThisToOnboardingDone()
            findNavController().navigate(act)
        }
    }

    private val restartBtnListener = OnClickListener {
        val restartIntent: Intent? = publicApplicationContext.packageManager
            .getLaunchIntentForPackage(publicApplicationContext.packageName)
        restartIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(restartIntent)
    }
}