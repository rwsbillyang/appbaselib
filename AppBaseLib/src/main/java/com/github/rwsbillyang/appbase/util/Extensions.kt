package com.github.rwsbillyang.appbase.util

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.github.rwsbillyang.appbase.BuildConfig
import es.dmoral.toasty.Toasty
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


fun ImageView.loadImg(url: String?, placeHolder: Drawable? = null,listener: RequestListener<Drawable>? = null) {
    if (url == null) {
        placeHolder?.let{
            this.setImageDrawable(it)
        }
    } else {
        Glide.with(this.context).load(url).listener(listener).into(this)
    }
}
var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

fun View.hide() {
    visible = false
}

fun View.show() {
    visible = true
}



enum class ToastType{
    SUCCESS, NORMAL, WARNING, ERROR
}

fun Context.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT, type: ToastType = ToastType.NORMAL) {
    when (type) {
        ToastType.WARNING -> Toasty.warning(this, msg, duration, true).show()
        ToastType.ERROR -> Toasty.error(this, msg, duration, true).show()
        ToastType.NORMAL -> Toasty.info(this, msg, duration, false).show()
        ToastType.SUCCESS -> Toasty.success(this, msg, duration, true).show()
    }
}
fun Fragment.toast(msg: CharSequence, duration: Int = Toast.LENGTH_SHORT, type: ToastType = ToastType.NORMAL) {
    if(this.context!=null)
    {
        when (type) {
            ToastType.WARNING -> Toasty.warning(this.context!!, msg, duration, true).show()
            ToastType.ERROR -> Toasty.error(this.context!!, msg, duration, true).show()
            ToastType.NORMAL -> Toasty.info(this.context!!, msg, duration, false).show()
            ToastType.SUCCESS -> Toasty.success(this.context!!, msg, duration, true).show()
        }
    }else
    {
        logw("no context associated with?")
    }

}

fun Activity.dispatchFailure(error: Throwable?) {
    error?.let {
        if (BuildConfig.DEBUG) {
            it.printStackTrace()
        }
        if (error is SocketTimeoutException) {
            it.message?.let { toast("网络连接超时", type = ToastType.ERROR) }

        } else if (it is UnknownHostException || it is ConnectException) {
            //网络未连接
            it.message?.let { toast("网络未连接", type = ToastType.ERROR) }

        } else {
            it.message?.let { toast(it, type = ToastType.ERROR) }
        }
    }
}

@Deprecated("Use navigation in Android architecture components instead")
inline fun AppCompatActivity.gotoFragment(fragment: Fragment,containerId:Int, pushStack: Boolean = true)
{

    if(pushStack){
        supportFragmentManager
            .beginTransaction()
            .replace(containerId,fragment)
            .addToBackStack(null)
            .commit()
    }else
    {
        supportFragmentManager
            .beginTransaction()
            .replace(containerId,fragment)
            .commit()
    }
}

@Deprecated("Use navigation in Android architecture components instead")
inline fun Fragment.gotoFragment(fragment: Fragment,containerId:Int, pushStack: Boolean = true)
{
    if(pushStack){
        requireFragmentManager()
            .beginTransaction()
            .replace(containerId,fragment)
            .addToBackStack(null)
            .commit()
    }else
    {
        requireFragmentManager()
            .beginTransaction()
            .replace(containerId,fragment)
            .commit()
    }
}