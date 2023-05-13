package run.jkdev.dec4iot.jetpack

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.navArgs

class BangleJsNotLikeThisFragment : Fragment() {
    val args: BangleJsNotLikeThisFragmentArgs by navArgs()

    private var fromIntent: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bangle_js_not_here, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fromIntent = args.fromIntent

        view.findViewById<TextView>(R.id.test).text = fromIntent.toString()

    }
}