package com.github.rwsbillyang.appbase.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.rwsbillyang.appbase.util.logw

abstract class BaseRecyclerViewAdapter<T,VH: RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>(){
    var list: MutableList<T> = ArrayList()


    override fun getItemCount() = list.size

    fun getItem(position: Int):T?{
        if (position < 0 || position >= list.size)
        {
            logw("IndexOutOfBounds,position=$position, list.size=${list.size}")
            return null
        }
        return list[position]
    }
    /**
     * 子类listAdapter需要指定item的layout，如R.layout.item_xxx
     */
    abstract fun getItemLayout(): Int

    /**
     * 子类listAdapter需要创建子类的viewHolder，如使用 XxxViewHolder();
     */
    abstract fun createViewHolder(itemView: View): VH

    /**
     * 指定新数据,会覆盖以前的老旧数据
     * */
    fun setData(newList: List<T>?) {
        resetList()
        if (!newList.isNullOrEmpty()) {
            list = newList.toMutableList()
            // log("newList data: ${newList.get(0).toString()}")
            this.notifyItemRangeInserted(0, newList.size)
        }
    }
    /**
     * 重置
     * */
    fun resetList() {
        val size = list.size
        this.list = ArrayList()
        this.notifyItemRangeRemoved(0,size)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(getItemLayout(), parent, false)
        return createViewHolder(view)
    }
}

/**
 * 简化RecyclerView.Adapter的使用，通常与PageHandler联合使用
 */
abstract class PageRecyclerViewAdapter<T,VH: RecyclerView.ViewHolder> : BaseRecyclerViewAdapter<T,VH>() {
    /**
     * 添加新数据，通常用于加载更多
     * */
    fun addListAtEnd(newList: List<T>?) {
        if (newList == null || newList.isEmpty()) {
            logw("newList is null or empty when addListAtEnd")
        } else {
            val position = list.size
            this.list =  this.list.plus(newList).toMutableList()
            this.notifyItemRangeInserted(position,newList.size)
        }
    }
    /**
     * 添加新数据，通常用于下拉刷新
     * */
    fun addListAtStart(newList: List<T>?) {
        if (newList == null || newList.isEmpty()) {
            logw("newList is null or empty when addListAtStart")
        } else {
            this.list =  newList.plus(this.list).toMutableList()
            this.notifyItemRangeInserted(0,newList.size)
        }
    }

}


/**
 * 简化RecyclerView.Adapter的使用
 *
 * https://developer.android.google.cn/reference/androidx/recyclerview/widget/AsyncListDiffer.html
 * https://github.com/googlesamples/android-architecture-components/blob/master/GithubBrowserSample/app/src/main/java/com/android/example/github/ui/common/DataBoundListAdapter.kt
 */
abstract class DiffRecyclerViewAdapter<T,VH: RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>):
    RecyclerView.Adapter<VH>() {
    val mDiffer = AsyncListDiffer(this, diffCallback)

    open fun getItem(position: Int):T?{
        if (position < 0 || position >= getItemCount())
        {
            logw("IndexOutOfBounds,position=$position, list.size=${getItemCount()}")
            return null
        }
        return mDiffer.getCurrentList().get(position)
    }

    /**
     * 子类listAdapter需要指定item的layout，如R.layout.item_xxx
     */
    abstract fun getItemLayout(): Int

    /**
     * 子类listAdapter需要创建子类的viewHolder，如使用 XxxViewHolder();
     */
    abstract fun createViewHolder(itemView: View):VH

    override fun getItemCount() = mDiffer.getCurrentList().size


    fun submitList(newList: List<T>?) {
        if(newList.isNullOrEmpty())
        {
            logw("null or empty list submitted, ignore")
            return
        }
        mDiffer.submitList(newList)
      //  this.notifyItemRangeInserted(0,newList.size)
    }

   open override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(getItemLayout(), parent, false)
        return createViewHolder(view)
    }
}



abstract class HeaderFooterAdapter<T,VH: RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>)
    :DiffRecyclerViewAdapter<T,VH>(diffCallback){

    companion object{
        const val VIEW_TYPE_ITEM = 0 // Normal list item
        const val VIEW_TYPE_HEADER = 1  // Header
        const val VIEW_TYPE_FOOTER = 2  // Footer
    }


    /**
     * Default header view is disabled, override in subclass and return true if want to enable it.
     */
    protected fun isHeaderEnabled() = false
    /**
     * @return Custom header view, but override [.isHeaderEnabled] and return true first.
     */
    protected fun createHeaderView(): View? {
        return null
    }

    /**
     * Default footer view is disabled, override in subclass and return true if want to enable it.
     */
    protected fun isFooterEnabled() = false
    /**
     * @return Custom footer view, but override [.isFooterEnabled] and return true first.
     */
    protected fun createFooterView(): View? {
        return null
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  VH {
//        return if (viewType == VIEW_TYPE_FOOTER) {
//            val header = createHeaderView()
//            if(header == null ) throw IllegalStateException("header view cannot be null")
//
//            object : RecyclerView.ViewHolder(header!!) {
//
//            }
//        }else if(viewType == VIEW_TYPE_HEADER){
//            object : RecyclerView.ViewHolder(createHeaderView()) {
//
//            }
//        }
//        else super.onCreateViewHolder(parent, viewType)
//    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            bindVH(holder, position)
        }
    }
    abstract fun bindVH(holder: VH, position: Int)

    override fun getItemViewType(position: Int): Int {
        return if(isHeaderEnabled() && position == 0){
            VIEW_TYPE_HEADER
        } else if (isFooterEnabled() && position == itemCount - 1) {
            VIEW_TYPE_FOOTER
        } else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        var itemCount = super.getItemCount()
        if (isHeaderEnabled()) {
            itemCount += 1
        }
        if (isFooterEnabled()) {
            itemCount += 1
        }
        return itemCount
    }

    override fun getItem(position: Int): T? {
        return  when(getItemViewType(position)){
            VIEW_TYPE_FOOTER -> null
            VIEW_TYPE_HEADER -> null
            VIEW_TYPE_ITEM -> super.getItem(position)
            else -> null
        }

    }
}
