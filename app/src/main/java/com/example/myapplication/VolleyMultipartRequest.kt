package com.example.myapplication

import com.android.volley.*
import com.android.volley.toolbox.*
import java.io.*
import java.nio.charset.Charset

abstract class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    private val headers = HashMap<String, String>()

    override fun getHeaders(): MutableMap<String, String> = headers

    override fun getBodyContentType(): String = "multipart/form-data; boundary=$boundary"

    abstract fun getByteData(): Map<String, DataPart>

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val writer = PrintWriter(OutputStreamWriter(bos, "UTF-8"), true)

        getByteData().forEach { (key, dataPart) ->
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"$key\"; filename=\"${dataPart.fileName}\"\r\n")
            writer.append("Content-Type: ${dataPart.type}\r\n\r\n")
            writer.flush()
            bos.write(dataPart.content)
            writer.append("\r\n")
            writer.flush()
        }

        writer.append("--$boundary--\r\n")
        writer.flush()

        return bos.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) {
        listener.onResponse(response)
    }

    data class DataPart(val fileName: String, val content: ByteArray, val type: String)

    private val boundary = "volley_boundary_${System.currentTimeMillis()}"
}
