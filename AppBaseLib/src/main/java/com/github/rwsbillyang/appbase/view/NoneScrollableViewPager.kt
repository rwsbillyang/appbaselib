package com.github.rwsbillyang.appbase.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NoneScrollableViewPager(context: Context, attrs: AttributeSet? = null): ViewPager(context, attrs){
    var noScroll = true

    override fun onTouchEvent(arg0: MotionEvent):Boolean = !noScroll && super.onTouchEvent(arg0)

    override fun onInterceptTouchEvent(arg0: MotionEvent):Boolean = !noScroll && super.onInterceptTouchEvent(arg0)

    //false 去除滚动效果
    override fun setCurrentItem(item: Int) = super.setCurrentItem(item, false)
}