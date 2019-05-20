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
</T> */
data class Resource<out T>(val status: Status, val data: T?, val message: String? = null, val bizCode: String? = null, val httpStatus:Int? = null) {
    companion object {
        fun <T> success(data: T?): Resource<T>
                = Resource(Status.OK, data)

        fun <T> bizErr(ret: String, msg: String?, data: T? = null)
                = Resource(Status.BIZ_KO, data, msg, ret)

        fun <T> err(msg: String, data: T? = null, httpStatus: Int? = null)
                = Resource(Status.ERR, data, msg, null, httpStatus)

        fun <T> loading(data: T? = null, msg: String? = null): Resource<T>
                = Resource(Status.LOADING, data, msg)

        fun <T> consumed(data: T? = null, msg: String? = null,ret: String? = null ,httpStatus: Int? = null)
                = Resource(Status.CONSUMED, data, msg,ret,httpStatus)
    }
}