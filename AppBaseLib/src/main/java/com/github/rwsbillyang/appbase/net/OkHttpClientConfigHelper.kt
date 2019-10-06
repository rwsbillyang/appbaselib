package com.github.rwsbillyang.appbase.net


import android.os.Build
import com.github.rwsbillyang.appbase.util.isIp
import com.github.rwsbillyang.appbase.util.logw
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager


object OkHttpClientConfigHelper {
    private val map: MutableMap<String, RetrofitTriple> = HashMap() //site baseUrl -> RetrofitTriple
    /**
     * 若提供全局的Http错误状态码处理器，则注册全局处理器
     * */
    private var errHandler: OnErrHandler? = null

    private var defaultConfig: ClientConfiguration = DefaultConfiguration()

    fun registerDefaultConfiguration(config: ClientConfiguration) {
        defaultConfig = config
    }

//    fun registerConfiguration(config: ClientConfiguration) {
//        var triple = map[config.host()]
//        if (triple != null) {
//            triple.config = config
//        } else {
//            triple = RetrofitTriple(config, null, null)
//            map[config.host()] = triple
//        }
//    }
    /**
     * 若提供全局的Http错误状态码处理器，则注册全局处理器
     * */
    fun registerGlobalErrHandler(errHandler: OnErrHandler)
    {
        this.errHandler = errHandler
    }

    fun clearCache() {
        map.clear()
    }


    private fun empty(interceptors: Array<Interceptor>?): Boolean {
        return interceptors?.isEmpty()?:true
    }


   // @JvmOverloads
    fun getRetrofit(clientConfig: ClientConfiguration, client: OkHttpClient): Retrofit {

        val host = clientConfig.host()

        val triple = map[host]
        if (triple?.retrofit != null) {
            return triple.retrofit!!
        }


        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create()

        //val client = getClient(host, clientConfig)

        val builder = Retrofit.Builder()
                .baseUrl(host)
                .client(client)
                //.addCallAdapterFactory(CoroutineCallAdapterFactory())
                //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))

        val retrofit = builder.build()
        if (triple == null)
            map[host] = RetrofitTriple(clientConfig, retrofit, client)
        else {
            triple.retrofit = retrofit
            triple.client = client
        }

        return retrofit
    }

    fun getClient(provider: ClientConfiguration): OkHttpClient {

        val client = map[provider.host()]?.client
        if (client != null) {
            return client
        }


        val builder = OkHttpClient.Builder()

        builder.connectTimeout(if (provider.connectTimeoutMs() != 0L)
            provider.connectTimeoutMs()
        else
            defaultConfig.connectTimeoutMs(), TimeUnit.MILLISECONDS)
        builder.readTimeout(if (provider.readTimeoutMs() != 0L)
            provider.readTimeoutMs()
        else
            defaultConfig.readTimeoutMs(), TimeUnit.MILLISECONDS)

        builder.writeTimeout(if (provider.writeTimeoutMs() != 0L)
            provider.writeTimeoutMs()
        else
            defaultConfig.writeTimeoutMs(), TimeUnit.MILLISECONDS)

        val cookieJar = provider.cookie()
        if (cookieJar != null) {
            builder.cookieJar(cookieJar)
        }

        val interceptors = provider.interceptors()

        if (!empty(interceptors)) {
            for (interceptor in interceptors!!) {
                builder.addInterceptor(interceptor)
            }
        }

        val netWorkInterceptors = provider.networkInterceptors()
        if (!empty(netWorkInterceptors)) {
            for (interceptor in netWorkInterceptors!!) {
                builder.addNetworkInterceptor(interceptor)
            }
        }

        if (provider.logEnable()) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }

        if (provider.gzipRequestEnable()) {
            builder.addNetworkInterceptor(GzipRequestInterceptor())
        }

        val headerMap = provider.requestHeaders()
        if (!headerMap.isNullOrEmpty()) {
            builder.addNetworkInterceptor(HeaderInterceptor(headerMap))
        }

        if(errHandler != null)
        {
            builder.addNetworkInterceptor(ErrorResponseInterceptor(errHandler!!))
        }


        //Android5.0版本以上以及一个类似先进网路服务器
        if (Build.VERSION.SDK_INT >= 22) {
            val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                ).build()
            builder.connectionSpecs(Collections.singletonList(spec))

        } else if (Build.VERSION.SDK_INT >= 16) {
            try {
                val specs = ArrayList<ConnectionSpec>()
                specs.add(ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2).build() )
                specs.add(ConnectionSpec.COMPATIBLE_TLS)
                specs.add(ConnectionSpec.CLEARTEXT)

                builder.connectionSpecs(specs)
            } catch (exc: Exception) {
                logw("Error while setting TLS 1.2, $exc.message")
            }

        } else {
            logw("Build.VERSION.SDK_INT=$(Build.VERSION.SDK_INT) , Not Support TLS")
        }

        if (provider.enableCustomTrust()) {
            if(provider.trustCertInputStreamList().isNullOrEmpty())
            {
               logw("you forget to configure your certificates resources?")
            }else
            {
                setupSSL(builder,provider)
            }
        }

        return builder.build()
    }

    /**
     * @param trustedInputStreamArray 受信任证书输入流
     * @param trustStorePwd 受信任证书密码
     *
     * @param clientCertInputStream 认证证书输入流，亦即客户端证书. 单向认证则传参为null
     * @param clientStorePwd 证书密码
     *
     * https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/CustomTrust.java
     * */
    private fun setupSSL(builder: OkHttpClient.Builder,provider:ClientConfiguration)
    {
        try {
            //val trustMangers = prepareTrustManagerFactory(trustedInputStreamArray[0], trustStorePwd).trustManagers
            val trustMangers = CustomCertificateHelper.prepareTrustManagerFactory(provider.trustCertInputStreamList()!!)

            if (trustMangers.size != 1 || trustMangers[0] !is X509TrustManager) {
                throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustMangers))
            }

            val keyManagers = CustomCertificateHelper.prepareKeyManager(provider.clientCertInputStream(),provider.clientPass(),provider.clientCertType())

            val sslSocketFactory = SSLContext.getInstance("TLS")
                .apply {
                    init(keyManagers,trustMangers,SecureRandom())
                }
                .socketFactory

            val mySocketFactory = if (Build.VERSION.SDK_INT in 16 until 22) Tls12SocketFactory(sslSocketFactory) else sslSocketFactory

            val trustManager = trustMangers[0]as X509TrustManager

            //对于服务器身份认证，若是IP地址访问，则跳过，无需认证
            builder.hostnameVerifier(object:HostnameVerifier{
                override fun verify(hostname:String, session: SSLSession)
                        =  hostname.isIp() || provider.host().matches(Regex("http(s)?://$hostname/"))
            }).sslSocketFactory(mySocketFactory, trustManager )

            //builder.hostnameVerifier { hostname, session -> true }

//            builder.hostnameVerifier{ hostname, session ->
//                hostname.isIp() || provider.host().matches(Regex("http(s)?://$hostname/"))
//            }.sslSocketFactory(mySocketFactory, trustManager )

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
    }

}
