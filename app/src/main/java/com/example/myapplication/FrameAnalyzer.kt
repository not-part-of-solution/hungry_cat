package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.android.volley.*
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class FrameAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {
    private var lastTimestamp = 0L
    private var dialogShown = false

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()

        if (now - lastTimestamp > 2500 && !dialogShown) { // 1 запрос каждые 2.5 секунды
            lastTimestamp = now

            val bitmap = imageProxyToBitmap(image)
            sendImageToServer(bitmap)
        }

        image.close()
    }

    private fun sendImageToServer(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val imageData = stream.toByteArray()

        val request = object : VolleyMultipartRequest(
            Method.POST,
            "https://server-for-me.onrender.com/predict", //
            Response.Listener { response ->
                val json = JSONObject(String(response.data))
                val label = json.getString("class")
                val confidence = json.getDouble("confidence")

                if (confidence > 0.6 && label != "unknown") {
                    dialogShown = true
                    showPopup(label, confidence)
                }
            },
            Response.ErrorListener {
                it.printStackTrace()
            }
        ) {
            override fun getByteData(): Map<String, DataPart> {
                return mapOf(
                    "image" to DataPart("frame.jpg", imageData, "image/jpeg")
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun showPopup(label: String, confidence: Double) {
        AlertDialog.Builder(context)
            .setTitle("Распознан объект")
            .setMessage("Имя: $label\nУверенность: ${(confidence * 100).toInt()}%")
            .setPositiveButton("ОК") { dialog, _ ->
                dialog.dismiss()
                dialogShown = false
            }
            .setCancelable(false)
            .show()
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        val success = yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)

        if (!success) {
            throw RuntimeException("JPEG compression failed")
        }

        val jpegBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }


}
