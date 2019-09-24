/*
 * Author: rwsbillyang@qq.com yangchanggang@gmail.com
 * Created At: 2019-09-21 09:13:21
 *
 * Copyright (c) 2019. All Rights Reserved.
 *
 */

package com.github.rwsbillyang.appbase.util

import java.util.regex.Pattern

/**
 * 是否是IP地址，只支持IPv4版本
 * */
fun String.isIp() =  Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").matcher(this).matches()