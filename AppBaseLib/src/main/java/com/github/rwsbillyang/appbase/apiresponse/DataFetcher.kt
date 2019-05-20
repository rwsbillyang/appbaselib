package com.github.rwsbillyang.appbase.apiresponse

import androidx.lifecycle.*
import com.github.rwsbillyang.appbase.NetAwareApplication
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.io.IOException
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 从远程返回的payload数据类型，从本地存储返回的数据类型，以及返回给调用者的数据类型均为T
 *
 * 这是最常用的情景
 * */
fun <T> dataFetcher(init: DataFetcher<T,T>.() -> Unit): LiveData<Resource<T>> {
    val fetcher = DataFetcher<T,T>()
    fetcher.init()
    fetcher.fetchData()
    return fetcher.asLiveData()
}

/**
 * RequestType表示从远程请求的payload数据类型，然后直接送往本地存储，故传递的数据类型也是RequestType
 * ResultType表示从本地存储中所取出来的payload类型，
 *
 * 最终DataFetcher返回的给调用者的payload数据类型是ResultType
 *
 * 当从远程返回的payload数据类型，需要变换时，用此函数
 * */
fun <RequestType,ResultType> dataFetcher2(init: DataFetcher<RequestType,ResultType>.() -> Unit): LiveData<Resource<ResultType>> {
    val fetcher = DataFetcher<RequestType,ResultType>()
    fetcher.init()
    fetcher.fetchData()
    return fetcher.asLiveData()
}
/**
 * RequestType表示从远程请求的payload数据类型，然后直接送往本地存储，故传递的数据类型也是RequestType
 * ResultType表示从本地存储中所取出来的payload类型，
 *
 * 最终DataFetcher返回的给调用者的payload数据类型是ResultType
 * */
class DataFetcher<RequestType,ResultType> {

    private val result = MediatorLiveData<Resource<ResultType>>()
    /**
     * 请求得到的数据储存于此，被viewModel中的LiveData观察
     * */
    fun asLiveData() = result as LiveData<Resource<ResultType>>

    /**
     * 是否异步请求, true表示同步，默认为false表示异步请求
     * */
    //  var sync = false

    /**
     * 添加上名字，便于调试知道是哪个dataFetcher在执行取数据操作
     * */
    var debugName: String = ""
    /**
     *
     * remoteAsBackend == false时，本地存储作为aside模式时：将以来自远程网络的数据为准，获取网络数据失败时才考虑本地数据
     *
     * 为true时，网络数据将作为本地数据的backend，网络数据先进本地存储，再对本地存储进行查询
     *
     * 为true时，只有在fromLocal和fromRemote都有效时，才能执行成功
     * */
    var enableRemoteAsBackend = false

    /**
     * 设置为true，则在新线程的协程里取数据；为false则在协程里取数据。
     * 若使用ROOM作为本地存储来说，必须设置为true，否则android报异常
     * */
    var enableCreateNewThread = true

    var enableMetrics = false

    private var localBlock: (() -> ResultType?)? = null
    /**
     * 从本地加载的代码块，返回的payload数据是ResultType，即最终返回给最终调用者所需要的数据类型
     *
     * 默认为空，表示不从本地获取数据，若激活，从网络请求的数据也将更新到本地
     * */
    fun fromLocal(block: () -> ResultType?) {
        localBlock = block
    }


    private var remoteBlock: (() -> Call<RequestType>)? = null
    /**
     * 从远程加载的代码块，返回的是一个Call调用，payload数据为RequestType
     * */
    fun fromRemote(block: () -> Call<RequestType>) {
        remoteBlock = block
    }


    private var saveBlock: ((RequestType) -> Int)? = null
    /**
     * 存入本地storage的代码块，将从远程网络返回的数据（类型为RequestType，若需转换请自行转换）传递给存储代码块
     * 默认为空不执行任何操作
     *
     * 保存影响的记录，若没有影响或出错则返回0，否则返回真实的影响记录
     * */
    fun save(block: (RequestType) -> Int) {
        saveBlock = block
    }


    /**
     * 将网络请求结果类型RequestType转换成UI最终所需的结果类型ResultType
     * 注意：传给localStorage进行save时的类型依然为RequestType
     * */
    private var converterBlock: ((RequestType?) -> ResultType?) = { if (it != null) it as ResultType else null }

    /**
     * 远程加载的代码块到实际所使用数据的转换器
     * 将远程加载获取的payload数据（RequestType类型）转换成ResultType，目的最终的UI调用者需求的是ResultType
     *
     * 默认的转换器是类型转换返回自身
     * */
    fun converter(block: (RequestType?) -> ResultType?) {
        converterBlock = block
    }


    /**
     * 将ApiSuccessResponse<RequestType>转换成RequestType类型
     * */
    private var processResponseBlock: ((ApiSuccessResponse<RequestType>) -> RequestType) = { it.body }

    /**
     * 将ApiSuccessResponse转换成RequestType，通常是response.body就是所需的payload数据，
     * 可以对远程返回的响应结果进行额外处理，但如果响应头中还有额外的payload数据，可以在此做额外的操作
     * */
    fun processResponse(block: (ApiSuccessResponse<RequestType>) -> RequestType) {
        processResponseBlock = block
    }


    /**
     * 是否强制刷新，即强制从网络获取数据,适用于remoteAsBackend=false
     * */
    private var isForceRefreshBlock: ((ResultType?) -> Boolean) = { true }

    /**
     * 是否强制刷新，即是否强制从远程获取最新数据
     *
     * 传递的数据通常是从本地存储中获取的数据，然后对其进行判断是否该获取最新的数据
     * */
    fun isForceRefresh(block: (ResultType?) -> Boolean) {
        isForceRefreshBlock = block
    }

    private var onFetchFailBlock: ((code: Int?, msg: String?) -> Unit)? = null
    /**
     * 从网络返回数据失败时的回调，默认为空不执行任何操作
     * */
    fun onFetchFail(block: (code: Int?, msg: String?) -> Unit) {
        onFetchFailBlock = block
    }

    /**
     * 本地内存缓存，特别适用于一个页面内的切换，无需再从数据库和远程查找时的情况
     *
     * 若本身结果为null值，将击穿缓存
     *
     * 激活开关并且提供cacheKey将生效
     * */
//    var enableMemoryCache = false
//    var cacheKey: String?  = null
//    private val memoryCache: ConcurrentMap<String, ResultType?> = ConcurrentHashMap()

    /**
     * 更新结果，更新后会通知UI/ViewModel中的监察者
     * */
    private fun setValue(newValue: Resource<ResultType>?) {
        if (newValue != null && result.value != newValue) {
           // Logger.i("$debugName notify data changed...${newValue.status}")
            result.postValue(newValue)
        }
    }


    /**
     * 先从本地获取数据，并通知给上层观察者；
     * 然后在支持刷新且网络可用时，再向网络请求数据，请求的结果告知interceptor
     * 当为aside模式时：再异步保存于本地，最后通知上层观察者
     * 当为remoteAsBackend模式时： 再同步保存到本地，若请求结果非正常，则额外通知上层观察者，最后再从本地获取数据。
     * 若网络不可用，先通知UI上层观察者
     * */
    fun fetchData() = CoroutineScope(IO).launch(
        if (enableCreateNewThread) newSingleThreadContext("io") else EmptyCoroutineContext
    )
    {
       // Logger.d("$debugName fetch data...")
        val localResult = fetchFromLocal()
        setValue(Resource.success(localResult))

        if (isForceRefreshBlock(localResult)) {
            if (NetAwareApplication.Instance.isNetworkAvailable()) {
                launch {
                    fetchFromRemote()?.let {
                        notifyInterceptors(it)
                        if (enableRemoteAsBackend) {
                            val res: Resource<ResultType> = it.toResource()
                            if (res.status != Status.OK) {
                                setValue(res) //非正常状态，告知UI层进行错误提示
                            } else {
                                Logger.d("$debugName save data into local...")
                                if (it.saveIntoLocalStorage() > 0)//有修改记录才再次查询本地数据并通知，否则无需再去查询
                                {
                                    Logger.d("$debugName load data from local after save...")
                                    setValue(Resource.success(fetchFromLocal()))
                                }
                            }
                        } else {
                            Logger.d("$debugName save data into local async...")
                            launch { it.saveIntoLocalStorage() }
                            setValue(it.toResource())
                        }
                    }
                }
            } else {
                setValue(Resource.err(NetAwareApplication.ifNetWorkUnavailableString))
                //if(enableRemoteAsBackend){ launch { setValue(Resource.success(localResult)) } }
            }
        }
    }

    class NullValue: Any()

    private suspend fun fetchFromLocal(): ResultType? {

        localBlock?.let {
            setValue(Resource.loading(null))
            Logger.d("$debugName fetch data from local...")
            if(enableMetrics)
            {
                val now = System.currentTimeMillis()
                val ret = withContext(IO) { localBlock!!() }
                val delta = System.currentTimeMillis()- now
                Logger.d("$debugName fetch data from local, spend $delta ms")

                return ret
            }else
            {
                return withContext(IO) { localBlock!!() }
            }
        }
        return null
    }

    private suspend fun fetchFromRemote(): ApiResponse<RequestType>? {
        remoteBlock?.let {
            setValue(Resource.loading(null))
            Logger.d("$debugName fetch data from remote...")
            if(enableMetrics){
                val now = System.currentTimeMillis()
                val ret = withContext(IO) { remoteBlock!!().makeApiResponse() }
                val delta = System.currentTimeMillis()- now
                Logger.d("$debugName fetch data from remote, spend $delta ms")

                return ret
            }
            else
            {
                return withContext(IO) { remoteBlock!!().makeApiResponse() }
            }
        }
        return null
    }

    private fun Call<RequestType>.makeApiResponse(): ApiResponse<RequestType> {
        return try {
            this.execute().let {
                Logger.d(
                    "$debugName response: message=${it.message()}, raw=${it.raw()}," +
                            " success=${it.isSuccessful},code=${it.code()},errBody=${it.errorBody()}"
                )
                if (it.isSuccessful)
                    ApiResponse.create(it)
                else {
                    ApiErrorResponse(it.message(), it.code())
                }
            }
        } catch (ioe: IOException) {
            val msg = "$debugName IOException: ${ioe.message}"
            Logger.w(msg)
            ApiErrorResponse(msg)
        } catch (e: RuntimeException) {
            val msg = "$debugName RuntimeException: ${e.message}"
            Logger.w(msg)
            ApiErrorResponse(msg)
        } catch (e: Exception) {
            val msg = "$debugName Exception: ${e.message}"
            Logger.w(msg)
            ApiErrorResponse(msg)
        }
    }

    private fun ApiResponse<RequestType>.saveIntoLocalStorage(): Int {
        if (saveBlock != null && this is ApiSuccessResponse) {
            val apiResponse = processResponseBlock(this)
            return saveBlock!!(apiResponse)
        }
        return 0
    }

    /**
     * TODO: 重新实现interceptor
     * 返回null则不更新liveData中的值，支持ResponseBox封装的值的提取处理
     * */
    private fun ApiResponse<RequestType>.toResource(): Resource<ResultType> {
        if (this.consumed) return Resource.consumed()

        return when (this) {
            is ApiEmptyResponse -> {
                Logger.w("$debugName got empty response from remote,correct?")
                 Resource.err("return nothing", null)
            }
            is ApiErrorResponse -> {
                onFetchFailBlock?.let { it(this.code, this.errorMessage) }
                Resource.err(this.errorMessage, null, this.code)
            }
            is ApiSuccessResponse -> {
                val apiResponse = processResponseBlock(this)
                Resource.success(converterBlock(apiResponse))
            }
        }
    }


    companion object {
        /**
         * 当有ApiResponse到来时，更新此liveData
         * 当为其添加observer时，可实现对ApiResponse的直接处理
         * */
        private val responseLiveData = MutableLiveData<ApiResponse<*>>()
        private val map: MutableMap<ApiResponseInterceptor,Observer<ApiResponse<*>>> = HashMap()
        /**
         * 为某种错误状态添加一个interceptor，用于直接对apiResponse的处理
         *
         * 若intercepter处理完返回true，表示已消耗掉，viewModel中的liveData将接受不到数据更新
         *
         * 不可重复添加
         * */
        fun addInterceptor(owner: LifecycleOwner, status: ApiResponseStatus, interceptor: ApiResponseInterceptor)
        {
            map[interceptor]?.let {
                Logger.w("interceptor exsit")
                return
            }

            val observer = Observer<ApiResponse<*>> {
                if(it.status == status ){
                    it.consumed = interceptor.intercept(it)
                }
            }
            map[interceptor] = observer
            responseLiveData.observe(owner,  observer)
        }

        /**
         * 取消一个interceptor
         * */
        fun removeInterceptor(interceptor: ApiResponseInterceptor)
        {
            map[interceptor]?.let {
                responseLiveData.removeObserver(it)
                map.remove(interceptor)
            }
        }

        /**
         * 通知interceptor，有ApiResponse到来
         * */
        fun notifyInterceptors(r: ApiResponse<*>){
            responseLiveData.postValue(r)
        }
    }

}

