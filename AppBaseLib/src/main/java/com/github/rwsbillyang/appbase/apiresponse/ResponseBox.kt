package com.github.rwsbillyang.appbase.apiresponse

/**
 *
 * 默认的业务结果,由返回码，错误信息msg，以及真实的数据结果构成
 * */
@Deprecated("Use http status code as buisiness err")
open class ResponseBoxx<T>(var ret: String, var msg: String? = null, var data: T? = null){
    open fun isOK() = "ok".equals(ret)
}