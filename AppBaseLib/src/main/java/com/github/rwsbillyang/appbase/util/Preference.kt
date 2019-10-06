package com.github.rwsbillyang.appbase.util

import android.content.Context
import com.github.rwsbillyang.appbase.NetAwareApplication
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 <pre>
 //使用示例，取值和赋值均自动由被委托者执行
 class ExampleActivity : AppCompatActivity(){
     var a: Int by Preference("a", 0)

     fun whatever(){
         println(a)//会从SharedPreference取这个数据
         aInt = 9 //会将这个数据写入SharedPreference
     }
 }
 </pre>
 *
 * */
class Preference<T>(val key: String, val default: T,val prefName: String = "default") : ReadWriteProperty<Any?, T> {

    val prefs by lazy { NetAwareApplication.Instance.getSharedPreferences(prefName, Context.MODE_PRIVATE) }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return findPreference(key, default)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(key, value)
    }

    private fun <U> findPreference(key: String, default: U): U = with(prefs) {
        val res: Any = when (default) {
            is Long -> getLong(key, default)
            is String -> getString(key, default)?:default
            is Int -> getInt(key, default)
            is Boolean -> getBoolean(key, default)
            is Float -> getFloat(key, default)
            else -> {
                //getString(name,null)
                throw IllegalArgumentException("This type can be saved into Preferences")
            }
        }

        res as U
    }

    private fun <U> putPreference(key: String, value: U) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            else -> {
               // putString(name, Gson().toJson(value))
               throw IllegalArgumentException("This type can be saved into Preferences")
            }
        }.apply()
    }

    /**
     * 删除全部数据
     */
    fun clearPreference(){
        prefs.edit().clear().commit()
    }

    /**
     * 根据key删除存储数据
     */
    fun clearPreference(key : String){
        prefs.edit().remove(key).commit()
    }

}
