package com.github.rwsbillyang.appbase.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.rwsbillyang.appbase.R
import com.github.rwsbillyang.appbase.apiresponse.Resource
import com.github.rwsbillyang.appbase.apiresponse.Status
import com.github.rwsbillyang.appbase.util.setVisible
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.loadingstate.*

/**
 * 绑定了loading_state.xml的Fragment
 * */
open class LoadingFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retry.setOnClickListener { retry() }
    }
    protected open fun retry(){}

    protected open fun renderLoading(resource: Resource<*>?)
    {
        Logger.i("updateLoading")

        val loadingVisible = resource?.status == Status.LOADING || resource?.status == Status.ERR
        loading.setVisible(loadingVisible)
        error_msg.setVisible(loadingVisible)
        retry.setVisible(resource?.status == Status.ERR)


        val progressvisible = resource?.status == Status.LOADING
        progress_bar.setVisible(progressvisible)
        if(progressvisible)
        {
            error_msg.text = getString(R.string.loading)
        }else if(resource?.status == Status.ERR) {
            error_msg.text = resource.message ?: resources.getString(R.string.unknown_error)
        }

    }
}