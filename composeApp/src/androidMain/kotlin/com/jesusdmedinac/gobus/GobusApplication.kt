package com.jesusdmedinac.gobus

import android.app.Application
import com.jesusdmedinac.gobus.di.KoinHelper

class GobusApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KoinHelper.initKoin()
    }
}
