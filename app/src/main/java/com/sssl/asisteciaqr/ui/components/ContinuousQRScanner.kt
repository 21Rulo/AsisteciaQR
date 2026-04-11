package com.sssl.asisteciaqr.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.google.zxing.BarcodeFormat

@Composable
fun ContinuousQRScanner(
    modifier: Modifier = Modifier,
    onQRScanned: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // Guardamos la referencia de la vista para poder controlarla
    var barcodeViewReference by remember { mutableStateOf<DecoratedBarcodeView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            DecoratedBarcodeView(context).apply {
                // Configurar solo para QR codes
                val formats = listOf(BarcodeFormat.QR_CODE)
                this.barcodeView.decoderFactory = DefaultDecoderFactory(formats)

                // Configuración de la cámara
                this.barcodeView.cameraSettings.apply {
                    isAutoFocusEnabled = true
                    isContinuousFocusEnabled = true
                }

                this.setStatusText("")

                // Callback que se ejecuta cada vez que detecta un QR
                val callback = object : BarcodeCallback {
                    private var lastScannedTime = 0L
                    private var lastScannedCode = ""
                    private val SCAN_DELAY = 1500L // 1.5 segundos de gracia entre el mismo QR

                    override fun barcodeResult(result: BarcodeResult?) {
                        result?.text?.let { qrContent ->
                            val currentTime = System.currentTimeMillis()

                            // Evitar escaneos duplicados muy rápidos del mismo QR
                            if (qrContent != lastScannedCode ||
                                (currentTime - lastScannedTime) > SCAN_DELAY) {

                                lastScannedCode = qrContent
                                lastScannedTime = currentTime

                                // Notificar el QR escaneado
                                onQRScanned(qrContent)
                            }
                        }
                    }
                }

                // Iniciar listener
                decodeContinuous(callback)

                // Guardamos la referencia
                barcodeViewReference = this

                // Forzamos el inicio de la cámara al crearse la vista
                resume()
            }
        }
    )

    // Manejar el ciclo de vida para que la cámara no se quede encendida en segundo plano
    DisposableEffect(lifecycleOwner, barcodeViewReference) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // ¡AQUÍ ESTÁ LA MAGIA! Encendemos la cámara
                    barcodeViewReference?.resume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    // Apagamos la cámara si la app pasa a segundo plano
                    barcodeViewReference?.pause()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Asegurarnos de apagar la cámara al destruir el componente
            barcodeViewReference?.pause()
        }
    }
}