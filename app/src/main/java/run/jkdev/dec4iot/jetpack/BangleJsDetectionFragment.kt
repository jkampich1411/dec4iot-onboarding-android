package run.jkdev.dec4iot.jetpack

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs

/**
 * A simple [Fragment] subclass.
 * Use the [BangleJsDetectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BangleJsDetectionFragment : Fragment() {
    private val args: BangleJsDetectionFragmentArgs by navArgs()

    var sensorId: String = ""
    var endpoint: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bangle_js_detection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.sensorId = args.sensorId
        this.endpoint = args.endpoint
    }
}