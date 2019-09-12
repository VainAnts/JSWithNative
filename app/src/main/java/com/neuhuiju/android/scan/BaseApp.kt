package com.neuhuiju.android.scan

import android.app.Application
import android.util.Log
import com.tencent.smtt.sdk.QbSdk


class BaseApp : Application() {
    init {
        instance = this
    }

    companion object {
        lateinit var instance: BaseApp

    }

    override fun onCreate() {
        super.onCreate()

        val cb = object : QbSdk.PreInitCallback {

            override fun onViewInitFinished(arg0: Boolean) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("BaseApp", " onViewInitFinished is $arg0")
            }

            override fun onCoreInitFinished() {
            }
        }
        //x5内核初始化接口
        QbSdk.initX5Environment(applicationContext, cb)
    }

    val getInstanse: BaseApp by lazy {
        this.getInstanse
    }
}