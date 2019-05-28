/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rwsbillyang.appbase.apiresponse



/**
 * Status of a resource that is provided to the UI.
 *
 *
 * These are usually created by the Repository classes where they return
 * `LiveData<Resource<T>>` to pass back the latest data to the UI with its fetch status.
 */
enum class Status {
    OK,
    BIZ_KO, //可能API返回是结果有误的消息
    ERR,
    LOADING,
    CONSUMED
}

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 * @param identifer: 表示发起调用的identifer，调用者发起调用时提供，然后Resource原样返回，
 * 可以标示出该Resource是哪次发起的调用的返回结果.
</T> */
data class Resource<out T>(val status: Status,
                           val identifier: Any?,
                           val data: T?,
                           val message: String? = null,
                           val bizCode: String? = null,
                           val httpStatus:Int? = null)
{
    companion object {
        fun <T> success(identifier: Any?, data: T?): Resource<T>
                = Resource(Status.OK, identifier,data)

//        fun <T> bizErr(ret: String, msg: String?, data: T? = null)
//                = Resource(Status.BIZ_KO, data, msg, ret)

        fun <T> err(identifier: Any?, msg: String, data: T? = null, httpStatus: Int? = null)
                = Resource(Status.ERR, identifier, data, msg, null, httpStatus)

        fun <T> loading(identifier: Any?, data: T? = null, msg: String? = null): Resource<T>
                = Resource(Status.LOADING, identifier, data, msg)

        fun <T> consumed(identifier: Any?, data: T? = null, msg: String? = null,ret: String? = null ,httpStatus: Int? = null)
                = Resource(Status.CONSUMED, identifier, data, msg,ret,httpStatus)
    }
}