package com.example.dogs.net

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

class UiLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val reqInfo = buildString {
            append("➡️ ${req.method} ${req.url}\n")
            // Si quieres headers:
            // req.headers.forEach { h -> append("${h.first}: ${h.second}\n") }
            req.body?.let { body ->
                try {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    val charset = body.contentType()?.charset(Charset.forName("UTF-8")) ?: Charsets.UTF_8
                    append("Body: ${buffer.readString(charset)}\n")
                } catch (_: Exception) { /* ignore */ }
            }
        }
        NetworkEvents.lastRequest.postValue(reqInfo)

        val res = chain.proceed(req)
        val peek = res.peekBody(1024 * 1024) // hasta 1MB para debug
        val resInfo = "⬅️ HTTP ${res.code} ${res.message}\n${peek.string()}"
        NetworkEvents.lastResponse.postValue(resInfo)

        return res
    }
}