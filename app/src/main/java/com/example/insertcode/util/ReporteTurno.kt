package com.example.insertcode.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.insertcode.data.EventoEntity
import com.example.insertcode.model.textoEstado
import java.io.File

// Genera un PDF real con el historial del turno (sin librerías externas)
object ReporteTurno {

    fun generar(context: Context, guardian: String, eventos: List<EventoEntity>): File {
        val anchoPag = 595
        val altoPag = 842
        val margen = 40f

        val titulo = Paint().apply { textSize = 18f; isFakeBoldText = true; color = Color.BLACK }
        val normal = Paint().apply { textSize = 11f; color = Color.BLACK }
        val gris = Paint().apply { textSize = 9.5f; color = Color.DKGRAY }

        val doc = PdfDocument()
        var numero = 1
        var pagina = doc.startPage(PdfDocument.PageInfo.Builder(anchoPag, altoPag, numero).create())
        var canvas = pagina.canvas
        var y = margen + 18f

        canvas.drawText("Insercode · Reporte de turno", margen, y, titulo)
        y += 22f
        canvas.drawText("Guardián: $guardian", margen, y, normal)
        y += 15f
        canvas.drawText("Generado: ${System.currentTimeMillis().formatoFechaHora()}", margen, y, normal)
        y += 15f
        canvas.drawText("Total de eventos: ${eventos.size}", margen, y, normal)
        y += 26f

        eventos.forEach { e ->
            if (y > altoPag - margen - 30f) {
                doc.finishPage(pagina)
                numero++
                pagina = doc.startPage(PdfDocument.PageInfo.Builder(anchoPag, altoPag, numero).create())
                canvas = pagina.canvas
                y = margen + 10f
            }
            canvas.drawText(
                "${e.fecha.formatoFechaHora()}  ${e.titulo}  [${e.estado.textoEstado()}]".take(95),
                margen, y, normal
            )
            y += 13f
            canvas.drawText(
                "${e.ubicacion} · ${e.detalle} · ${e.camara}".take(110),
                margen + 10f, y, gris
            )
            y += 19f
        }
        doc.finishPage(pagina)

        val dir = File(context.cacheDir, "reportes").apply { mkdirs() }
        val archivo = File(dir, "reporte_turno_${System.currentTimeMillis()}.pdf")
        archivo.outputStream().use { doc.writeTo(it) }
        doc.close()
        return archivo
    }

    // Abre el selector de apps para compartir / guardar el PDF
    fun compartir(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir reporte de turno"))
    }
}
