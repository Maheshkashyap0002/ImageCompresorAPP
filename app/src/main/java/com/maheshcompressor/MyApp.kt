package com.maheshcompressor

import android.app.Application
import com.google.android.gms.ads.MobileAds


class MyApp : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this)

        appOpenAdManager = AppOpenAdManager(this)
        appOpenAdManager.loadAd()
    }
}