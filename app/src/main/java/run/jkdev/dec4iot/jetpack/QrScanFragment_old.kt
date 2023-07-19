package run.jkdev.dec4iot.jetpack

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.barcode.common.Barcode
import run.jkdev.dec4iot.jetpack.qrcode.AnalysisUseCase
import run.jkdev.dec4iot.jetpack.qrcode.PreviewUseCase
import run.jkdev.dec4iot.jetpack.qrcode.Scanner

class QrScanFragment_old : Fragment() {

    private var alreadyScanned = false

    private val qrOnSuccess = OnSuccessListener<MutableList<Barcode>> {
        if(it.isEmpty()) { return@OnSuccessListener }
        if(this.alreadyScanned) { return@OnSuccessListener }

        val code = it.getOrNull(0)

        //val act = QrScanFragmentDirections.actionQrScanFragmentToQrSuccessFragment(code!!.rawValue.toString())
        //findNavController().navigate(act)

        this.alreadyScanned = true
    }

    private fun startScanning() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity().applicationContext)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val surfaceProvider = requireView().findViewById<PreviewView>(R.id.qr_previewSurface).surfaceProvider

            // Create the UseCase Preview
            val casePreview = PreviewUseCase(surfaceProvider).case

            // Create the Analysis UseCase
            val caseAnalysis = AnalysisUseCase(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).case {
                    imageProxy -> Scanner().processImageProxy(imageProxy, this@QrScanFragment_old.qrOnSuccess)
            }

            val selectedCamera = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, selectedCamera,
                    casePreview, caseAnalysis
                )

            } catch (e: Exception) {
                Log.e(TAG, "Camera Binding Failed", e)
            }

        }, ContextCompat.getMainExecutor(requireActivity().applicationContext))
    }

    override fun onViewCreated(viewI: View, savedInstanceState: Bundle?) {
        super.onViewCreated(viewI, savedInstanceState)

        startScanning()
    }
}