package com.github.rwsbillyang.appbase.util

import android.util.Log
import com.github.rwsbillyang.appbase.BuildConfig

object LogTag{
    var Tag: String? = null
}
fun Any.log(message: String) {
    if (BuildConfig.DEBUG) Log.i(LogTag.Tag ?: this::class.simpleName, message)
}
fun Any.logw(message: String) {
    if (BuildConfig.DEBUG) Log.w(LogTag.Tag ?: this::class.simpleName, message)
}

fun Any.log(error: Throwable) {
    if (BuildConfig.DEBUG)  Log.e(LogTag.Tag ?: this::class.simpleName,error.message ?: "Error", error)
}

fun Any.log(message: String, error: Throwable) {
    if (BuildConfig.DEBUG) Log.e(LogTag.Tag ?: this::class.simpleName, message, error)
}