package com.github.rwsbillyang.appbase.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.rwsbillyang.appbase.util.logw


/**
 * 简化RecyclerView.Adapter的使用，通常与PageHandler联合使用
 */
abstract class BaseRecyclerViewAdapter<ItemType,VH: RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    private var list: List<ItemType> = ArrayList()

    val items get() = list

    fun getItem(position: Int):ItemType?{
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

    override fun getItemCount() = list.size

    /**
     * 添加新数据，通常用于加载更多
     * */
    fun addListAtEnd(newList: List<ItemType>?) {
        if (newList == null || newList.isEmpty()) {
            logw("newList is null or empty when addListAtEnd")
        } else {
            val position = list.size
            this.list =  this.list.plus(newList)
            this.notifyItemRangeInserted(position,newList.size)
        }
    }
    /**
     * 添加新数据，通常用于下拉刷新
     * */
    fun addListAtStart(newList: List<ItemType>?) {
        if (newList == null || newList.isEmpty()) {
            logw("newList is null or empty when addListAtStart")
        } else {
            val position = list.size
            this.list =  newList.plus(this.list)
            this.notifyItemRangeInserted(0,newList.size)
        }
    }
    /**
     * 指定新数据,会覆盖以前的老旧数据
     * */
    fun setList(newList: List<ItemType>?)
    {
        resetList()
        if (!newList.isNullOrEmpty()) {
            this.list = newList
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
 * 简化RecyclerView.Adapter的使用
 *
 * https://developer.android.google.cn/reference/androidx/recyclerview/widget/AsyncListDiffer.html
 * https://github.com/googlesamples/android-architecture-components/blob/master/GithubBrowserSample/app/src/main/java/com/android/example/github/ui/common/DataBoundListAdapter.kt
 */
abstract class BaseRecyclerViewAdapterDiff<ItemType,VH: RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<ItemType>):
    RecyclerView.Adapter<VH>() {
    private val mDiffer = AsyncListDiffer(this, diffCallback)

    /**
     * 子类listAdapter需要指定item的layout，如R.layout.item_xxx
     */
    abstract fun getItemLayout(): Int

    /**
     * 子类listAdapter需要创建子类的viewHolder，如使用 XxxViewHolder();
     */
    abstract fun createViewHolder(itemView: View):VH

    override fun getItemCount() = mDiffer.getCurrentList().size


    fun submitList(newList: List<ItemType>?) {
        mDiffer.submitList(newList)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(getItemLayout(), parent, false)
        return createViewHolder(view)
    }
}

