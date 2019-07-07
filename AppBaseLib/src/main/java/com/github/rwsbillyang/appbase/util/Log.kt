package com.github.rwsbillyang.appbase.util

import android.util.Log
import com.github.rwsbillyang.appbase.BuildConfig

object LogConfig{
    /**
     * 指定了统一的标签的话，同时在message前缀中添加classname
     * */
    var Tag: String? = null
        set(value)  {
            field = value
            if(!value.isNullOrBlank()) isShowClassName = true
        }

    var isShowClassName = false
}
fun Any.log(message: String) {
    if (BuildConfig.DEBUG) Log.i(LogConfig.Tag ?: this::class.simpleName,
        if(LogConfig.isShowClassName) this::class.simpleName +"|" + message else message)
}
fun Any.logw(message: String) {
    if (BuildConfig.DEBUG) Log.w(LogConfig.Tag ?: this::class.simpleName,
        if(LogConfig.isShowClassName) this::class.simpleName +"|" + message else message)
}

fun Any.log(error: Throwable) {
    if (BuildConfig.DEBUG){
        val msg = error.message ?: "Error"
        Log.e(LogConfig.Tag ?: this::class.simpleName,
            if(LogConfig.isShowClassName) this::class.simpleName +"|" + msg else msg)
    }
}

fun Any.log(message: String, error: Throwable) {
    if (BuildConfig.DEBUG) Log.e(LogConfig.Tag ?: this::class.simpleName,
        if(LogConfig.isShowClassName) this::class.simpleName +"|" + message else message
        , error)
}