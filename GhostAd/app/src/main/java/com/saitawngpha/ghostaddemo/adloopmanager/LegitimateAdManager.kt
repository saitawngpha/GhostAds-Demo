package com.saitawngpha.ghostaddemo.adloopmanager

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * **LEGITIMATE USAGE**: Single ad, user-initiated, proper lifecycle
 */
class LegitimateAdManager(private val activity: Activity) {
    private var interstitialAd: InterstitialAd? = null

    fun loadAd() {
        // Load ONE ad
        InterstitialAd.load(activity, "ca-app-pub-3940256099942544/1033173712", AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    // Show button to user - THEY decide when to see ad
                }
            }
        )
    }

    fun showAd() {
        // **USER-INITIATED**: Only show when user clicks button
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null // Load new ad only after dismissal
                }
            }
            ad.show(activity)
        }
    }
}
