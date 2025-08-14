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
        val peek = res.peekBody(1024 * 1024)
        val resInfo = "⬅️ HTTP ${res.code} ${res.message}\n${peek.string()}"
        NetworkEvents.lastResponse.postValue(resInfo)

        return res
    }
}