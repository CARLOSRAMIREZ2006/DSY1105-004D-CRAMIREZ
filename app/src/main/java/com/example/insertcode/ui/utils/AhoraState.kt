package com.example.insertcode.ui.utils

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import java.io.File

// Hora actual (ms) que se refresca cada "cadaMs": relojes y contadores "hace X min"
@Composable
fun rememberAhora(cadaMs: Long = 1000L): State<Long> =
    produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(cadaMs)
            value = System.currentTimeMillis()
        }
    }

// Uri de archivo temporal (FileProvider) para fotografiar con la cámara del teléfono
fun crearUriTemporal(context: Context): Uri {
    val directory = File(context.cacheDir, "images").apply { mkdirs() }
    val tempFile = File.createTempFile("picture_${System.currentTimeMillis()}", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}
