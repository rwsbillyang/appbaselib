package com.github.rwsbillyang.appbase.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.rwsbillyang.appbase.apiresponse.Resource
import com.github.rwsbillyang.appbase.apiresponse.Status


enum class LoadDirection(val vaule:Int){
    Previous(-1),Initial(0), Next(1)
}
/**
 * 根据PageHandlerState可恢复某个PageHandler的状态：当前页以及是否还可加载更多
 * */
data class PageHandlerState(var hasMore: Boolean = true, var currentPage: Int = 0){
    fun reset(){
        hasMore = true
        currentPage = 0
    }
}
/**
 * 上拉加载更多（LoadDirection.Next）和下拉更新(LoadDirection.Previous)分别需要创建不同的PageHandler，
 * 然后调用loadPage进行加载,然后UI观察pageResult，对结果进行对应的展示
 *
 * 对于起始页，也可以使用LoadDirection.Initial进行加载
 *
 * @param direction 加载数据的方向
 * @param currentPage 正在进行的当前页,一旦传入初始值，后续的当前值由PageHandler维护。
 * 当加载更多成功后，会+1,下次再加载更多，会在新的currentPage基础上进行
 * 当加载最新成功后，会-1，继续下拉刷新，则会在新的currentPage基础上进行
 * @param doLoadPage 加载下一个页面的lamada
 *
 *
 * <pre>

 * </pre>
 * */
open class PageHandler<QueryParameterType,ResultType>(
    val direction: LoadDirection,
    var currentPage: Int,
    val doLoadPage: (query:QueryParameterType, page: Int, identifier: Any?) -> LiveData<Resource<List<ResultType>>>
): Observer<Resource<List<ResultType>>> {
    private var _loadingResultLiveData: LiveData<Resource<List<ResultType>>>? = null
    private var _hasMore: Boolean = true

    fun getState() = PageHandlerState(_hasMore,currentPage)
    fun restoreState(state: PageHandlerState){
        _hasMore = state.hasMore
        currentPage = state.currentPage
    }
    fun resetState(){
        _hasMore = true
        currentPage = 0
    }

    /**
     * 使用者观察结果
     * */
    var pageList: MutableLiveData<Resource<List<ResultType>>> = MutableLiveData()

    /**
     * 使用者观察结果, 如保存变化的page
     * */
    var pageIndex: MutableLiveData<Resource<Int>> = MutableLiveData()

    private var isloadingPage = false

    //abstract fun doLoadPage(query:QueryParameterType, page: Int):LiveData<Resource<List<ResultType>>>?

    /**
     * 加载，通过观察pageList得到结果。如果返回true表示加载进行中，返回false表示因各种原因未进行加载
     *
     * 页码变化通知通过观察pageIndex而获得
     * */
    fun loadPage(query: QueryParameterType,identifier: Any?):Boolean {
        if(!_hasMore)
        {
            logw("no more data, ignore")
            pageList.value = Resource.err(identifier,"no more data")
            return false
        }
        if(isloadingPage){
            logw("is loading ")
            return false
        }

        var page = currentPage
        if(direction == LoadDirection.Previous)
        {
            if(currentPage <= 0)
            {
                logw("has reached the first page,no more data,currentPage=$currentPage")
                return false
            }else
            {
                page -= 1
                log("to load previous page=$page")
            }
        }else if(direction == LoadDirection.Next)
        {
            page += 1
            log("to load next page=$page")
        }else
        {
            log("to load current page=$page")
        }
        isloadingPage = true

        unregister()

        _loadingResultLiveData = doLoadPage(query,page,identifier)
        _loadingResultLiveData?.observeForever(this)
        return true
    }

    private fun unregister() {
        _loadingResultLiveData?.removeObserver(this)
        _loadingResultLiveData = null
    }

    fun reset() {
        unregister()
        _hasMore = true
        isloadingPage = false
    }

    override fun onChanged(result: Resource<List<ResultType>>?) {
        if (result == null) {
            logw("pageHandler loadPage callback, should not return null")
            reset()
        } else {
            pageList.value = result
            when (result.status) {
                Status.LOADING -> {
                    // ignore
                }
                Status.OK -> {
                    unregister()
                    isloadingPage = false
                    _hasMore = result.data?.isNotEmpty()?: false
                    currentPage = currentPage + direction.vaule
                    log("send pageVaule $currentPage, identifier:${result.identifier}")
                    pageIndex.value = Resource.success(result.identifier,currentPage)
                }
                else -> {
                    unregister()
                    _hasMore = true
                    isloadingPage = false
                }
            }

        }
    }
}