package com.github.rwsbillyang.appbase.net

import android.app.Application
import okhttp3.CookieJar
import okhttp3.Interceptor
import java.io.InputStream


interface ClientConfiguration
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
     * 是否激活自定义受信任证书，服务器身份证书
     * */
    fun enableCustomTrust(): Boolean = false

    /**
     * 服务器身份证书输入流列表，必须打开开关enableCustomTrust
     * 须pem格式，即crt文件中内容
     * */
    fun trustCertInputStreamList(): List<InputStream>? = null


    /**
     * 用于双向认证，服务器端需要对客户端认证时，客户端身份证书输入流，必须打开开关enableCustomTrust
     * 须 PKCS12或BKS 等android支持的文件输入流
     * */
    fun clientCertInputStream():InputStream? = null

    /**
     * 客户端证书密码
     * */
    fun clientPass():String? = null

    /**
     * 客户端证书输入流格式
     * */
    fun clientCertType() :String = "PKCS12"

    /**
     * 设置自定义证书文件，用于转换
     * */
    fun convertCertificatesReources(application: Application, array: Array<Int>)
            = array.map { application.resources.openRawResource(it) }

 }