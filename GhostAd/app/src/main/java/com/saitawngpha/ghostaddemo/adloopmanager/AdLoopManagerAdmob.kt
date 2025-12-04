package com.saitawngpha.ghostaddemo.adloopmanager

import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.*
import java.util.*

/**
 * **MALICIOUS VARIANT**: Uses real AdMob SDK for fraud
 * This violates AdMob policies and will get your account banned
 */
class AdLoopManagerAdmob(private val context: Context) {
    private var adLoopJob: Job? = null
    private val adScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentInterstitialAd: InterstitialAd? = null

    // **VIOLATION**: Using test ads ID for demonstration - real malware would use real ad unit IDs
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Google's test ID

    /**
     * **FRAUDULENT**: Endless loop loading real ads in background
     */
    suspend fun startAdLoop() {
        // Initialize AdMob (normally done once in Application class)
        MobileAds.initialize(context)

        adLoopJob = adScope.launch {
            while (isActive) {
                try {
                    // **VIOLATION**: Loading ads without user interaction
                    loadNextInterstitialAd()

                    // **VIOLATION**: Rapid-fire loading (6s is way too frequent)
                    delay(6000L)

                } catch (e: Exception) {
                    // Silent failure to avoid detection
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * **MALICIOUS**: Loads ad but never shows it to user
     * Generates impressions fraudulently
     */
    private fun loadNextInterstitialAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    currentInterstitialAd = ad

                    // **FRAUD START** - This is the malicious part

                    // **TECHNIQUE 1**: Show ad to invisible WebView
                    // Invisible WebView is created off-screen or with 0x0 size
                    // showAdInvisibly(ad)

                    // **TECHNIQUE 2**: Manually fire impression pixel
                    // Extract tracking URLs from ad metadata and call them
                    // fireImpressionPixelsManually(ad)

                    // **TECHNIQUE 3**: Show and immediately dismiss
                    // showAndDismiss(ad)

                    // **TECHNIQUE 4**: Most advanced - Reflection to access internal metrics
                    // Manually increment impression counters without showing
                    // simulateImpressionViaReflection(ad)

                    // For demo, we'll just log (real malware would do one of above)
                    println("MALICIOUS: AdMob interstitial loaded: ${ad.responseInfo}")

                    // **FRAUD END**
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    println("Ad load failed: ${error.message}")
                }
            }
        )
    }

    /**
     * **MALICIOUS TECHNIQUE**: Show ad off-screen
     * Creates invisible activity or uses system overlay windows
     */
    private fun showAdInvisibly(ad: InterstitialAd) {
        // This requires SYSTEM_ALERT_WINDOW permission
        // Creates 1x1 pixel window to "show" ad

        // **CRITICAL**: This violates AdMob policy on hidden ads
        // and Google's System Interference policy

        // Real implementation would involve:
        // 1. Creating a dummy Activity with transparent theme
        // 2. Setting Window to 1x1 pixel size
        // 3. Showing ad in that invisible window
        // 4. Auto-closing after impression registers

        // Not providing full code to prevent copy-paste abuse
    }

    /**
     * **MALICIOUS TECHNIQUE**: Manually fire tracking URLs
     * Extracts impression pixels from ad object and calls them directly
     */
    private fun fireImpressionPixelsManually(ad: InterstitialAd) {
        // Real malware uses reflection to access ad.responseInfo.responseUrl
        // or extracts URLs from ad metadata, then makes HTTP requests

        // This bypasses the SDK's legitimate show() flow
        // and directly reports impressions to AdMob's servers

        // Example pseudo-code:
        // val impressionUrl = extractImpressionUrl(ad)
        // httpClient.get(impressionUrl)
    }

    fun stopAdLoop() {
        adLoopJob?.cancel()
        adScope.cancel()
        currentInterstitialAd = null
    }
}

/**
 * **DETECTION MECHANISMS** (Why this fails in production)
 *
 * 1. **Viewability Tracking**: AdMob SDK has internal viewability measurement.
 *    - Invisible ads don't register as "viewed"
 *    - Impressions are invalidated if <50% of ad is visible for <1 second
 *    - Screen recording analysis detects off-screen rendering
 *
 * 2. **Timing Analysis**: ML models detect unnatural patterns
 *    - Loading ads every 6 seconds is flagged
 *    - Immediate load-show-dismiss sequences are detected
 *    - Human interaction patterns are expected
 *
 * 3. **Device Fingerprinting**:
 *    - High impression volume from same device ID
 *    - Low conversion rates (no real clicks)
 *    - Battery/data usage anomalies
 *
 * 4. **SDK Tampering Detection**:
 *    - AdMob SDK detects if show() was called but not rendered
 *    - Checks for root, Xposed, Frida (common in malware)
 *    - Validates app signatures and integrity
 *
 * 5. **Policy Violations**:
 *    - Apps banned for "Invalid Traffic" within hours
 *    - Google Play Protect blocks the app
 *    - Developer accounts terminated permanently
 */