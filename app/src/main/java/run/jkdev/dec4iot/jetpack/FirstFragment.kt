package run.jkdev.dec4iot.jetpack

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import run.jkdev.dec4iot.jetpack.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i(TAG, requireActivity().intent.action.toString())

        binding.buttonFirst.setOnClickListener {
            val isFromIntent: Boolean = requireActivity().intent.action == "me.byjkdev.dec4iot.intents.banglejs.SETUP"

            val act = FirstFragmentDirections.actionFirstFragmentToSecondFragment(isFromIntent)
            findNavController().navigate(act)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}