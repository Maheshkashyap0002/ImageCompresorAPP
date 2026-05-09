package com.maheshcompressor


import android.app.Activity
import android.app.Application
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(private val application: Application) {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false

    private val adUnitId = "ca-app-pub-3940256099942544/9257395921" // 🔥 Replace with REAL ID

    fun loadAd() {
        if (isLoadingAd || appOpenAd != null) return

        isLoadingAd = true

        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            application,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoadingAd = false
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity) {


        if (isPremiumUser(activity)) return

        if (isShowingAd) return

        // If already showing → do nothing
        if (isShowingAd) return

        // If ad not ready → load it and exit
        if (appOpenAd == null) {
            loadAd()
            return
        }

        appOpenAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    loadAd() // 🔥 preload next
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    loadAd()
                }
            }

        appOpenAd?.show(activity)
    }
}