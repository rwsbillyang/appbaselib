package com.github.rwsbillyang.appbase.net

import okhttp3.OkHttpClient
import retrofit2.Retrofit

data class RetrofitTriple(var config: ClientConfiguration,
                          var retrofit: Retrofit?,
                          var client: OkHttpClient?)