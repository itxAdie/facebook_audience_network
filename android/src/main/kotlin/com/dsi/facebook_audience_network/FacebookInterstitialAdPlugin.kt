package com.dsi.facebook_audience_network

import android.content.Context
import android.os.Handler
import android.util.Log
import com.facebook.ads.*
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class FacebookInterstitialAdPlugin(
    private val context: Context,
    private val channel: MethodChannel
) : MethodChannel.MethodCallHandler, InterstitialAdListener {

    private var interstitialAd: InterstitialAd? = null
    private val delayHandler = Handler()

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            FacebookConstants.SHOW_INTERSTITIAL_METHOD -> result.success(showAd(call.arguments as HashMap<*, *>))
            FacebookConstants.LOAD_INTERSTITIAL_METHOD -> result.success(loadAd(call.arguments as HashMap<*, *>))
            FacebookConstants.DESTROY_INTERSTITIAL_METHOD -> result.success(destroyAd())
            else -> result.notImplemented()
        }
    }

    private fun loadAd(args: HashMap<*, *>): Boolean {
        val placementId = args["id"] as? String ?: return false

        if (interstitialAd == null) {
            interstitialAd = InterstitialAd(context, placementId)
        }

        return try {
            if (interstitialAd?.isAdLoaded != true) {
                val loadConfig = interstitialAd!!
                    .buildLoadAdConfig()
                    .withAdListener(this)
                    .withCacheFlags(CacheFlag.ALL)
                    .build()
                interstitialAd!!.loadAd(loadConfig)
            }
            true
        } catch (e: Exception) {
            Log.e("InterstitialLoadAdError", e.message ?: "Unknown error")
            false
        }
    }

    private fun showAd(args: HashMap<*, *>): Boolean {
        val delay = (args["delay"] as? Int) ?: 0

        val ad = interstitialAd ?: return false
        if (!ad.isAdLoaded || ad.isAdInvalidated) return false

        val showAdConfig = ad.buildShowAdConfig().build()

        if (delay <= 0) {
            ad.show(showAdConfig)
        } else {
            delayHandler.postDelayed({
                if (ad.isAdLoaded && !ad.isAdInvalidated) {
                    ad.show(showAdConfig)
                }
            }, delay.toLong())
        }

        return true
    }

    private fun destroyAd(): Boolean {
        interstitialAd?.destroy()
        interstitialAd = null
        return true
    }

    override fun onInterstitialDisplayed(ad: Ad) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.DISPLAYED_METHOD, args)
    }

    override fun onInterstitialDismissed(ad: Ad) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.DISMISSED_METHOD, args)
    }

    override fun onError(ad: Ad?, adError: AdError) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to (ad?.placementId ?: ""),
            "invalidated" to (ad?.isAdInvalidated ?: false),
            "error_code" to adError.errorCode,
            "error_message" to adError.errorMessage
        )
        channel.invokeMethod(FacebookConstants.ERROR_METHOD, args)
    }

    override fun onAdLoaded(ad: Ad) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.LOADED_METHOD, args)
    }

    override fun onAdClicked(ad: Ad) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.CLICKED_METHOD, args)
    }

    override fun onLoggingImpression(ad: Ad) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.LOGGING_IMPRESSION_METHOD, args)
    }
}
