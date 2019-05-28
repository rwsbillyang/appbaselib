package com.github.rwsbillyang.appbase.net


import com.github.rwsbillyang.appbase.util.logw
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ErrorResponseInterceptor(private var errHandler: OnErrHandler): Interceptor{

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {


        val response = chain.proceed(chain.request())
        //Returns true if the code is in [200..300), which means the request was successfully received, understood, and accepted.
        if(response.isSuccessful)
        {
            return response
        }else
        {
            //出现诸如404，500之内的错误, refer to ErrorMap
            when(val code = response.code())
            {
                in ErrorCodes ->{
                    val msg = response.request().url().encodedPath() + " return " + (ErrorMap[code])?: response.message()
                    logw(msg)
                    errHandler(code, msg)
                }
            }
        }

        return response
    }

}