package com.github.rwsbillyang.appbase.net

import android.app.Application
import okhttp3.CookieJar
import okhttp3.Interceptor
import java.io.InputStream


interface NetConfiguration
{
    /**
     * 主机地址，如 "http://localhost/"
     * */
    fun host(): String
    /**
     * 提供application Interceptor，无需日志和gzip 类型Interceptor，直接激活即可
     * */
    fun interceptors(): Array<Interceptor>? = null

    /**
     * 提供network类型Interceptor，无需日志和gzip 类型Interceptor，直接激活即可
     * */
    fun networkInterceptors(): Array<Interceptor>? = null

    /**
     * 若添加了请求头,比如userAgent，将自动配置HeaderInterceptor
     * */
    fun requestHeaders():Map<String, String>? = null

    /**
     * 是否激活日志Interceptor
     * */
    fun logEnable(): Boolean = true

    /**
     * 是否激活压缩Interceptor
     * */
    fun gzipRequestEnable() = true

    //fun configHttps(builder: OkHttpClient.Builder) = {}

    fun cookie(): CookieJar? = null

    fun connectTimeoutMs(): Long = 20 * 1000L

    fun readTimeoutMs(): Long = 10 * 1000L

    fun writeTimeoutMs(): Long = 10 * 1000L

    /**
     * 激活TLS1.2
     * */
    fun enableMordenTLS(): Boolean = true

    /**
     * 自定义证书
     * */
    fun enableCustomTrust(): Boolean = false

    /**
     * 参考实现参见 DefaultConfiguration
     * */
    fun cetrificatesInputStreamList(): List<InputStream>? = null

    /**
     * keystore passwd
     * */
    fun passwd(): String? = null
    /**
     * 设置自定义证书文件
     * */
    fun convertCertificatesReources(application: Application, array: IntArray?)
            = array?.map { application.resources.openRawResource(it) }

 }