package com.dsi.facebook_audience_network

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class FacebookRewardedVideoAdPlugin(
    private val context: Context,
    private val channel: MethodChannel
) : MethodChannel.MethodCallHandler, RewardedVideoAdListener {

    private var rewardedVideoAd: RewardedVideoAd? = null
    private val delayHandler = Handler(Looper.getMainLooper())

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            FacebookConstants.SHOW_REWARDED_VIDEO_METHOD -> {
                @Suppress("UNCHECKED_CAST")
                val args = call.arguments as? HashMap<String, Any> ?: hashMapOf()
                result.success(showAd(args))
            }
            FacebookConstants.LOAD_REWARDED_VIDEO_METHOD -> {
                @Suppress("UNCHECKED_CAST")
                val args = call.arguments as? HashMap<String, Any> ?: hashMapOf()
                result.success(loadAd(args))
            }
            FacebookConstants.DESTROY_REWARDED_VIDEO_METHOD -> {
                result.success(destroyAd())
            }
            else -> result.notImplemented()
        }
    }

    private fun loadAd(args: HashMap<String, Any>): Boolean {
        val placementId = args["id"] as? String ?: return false
        if (rewardedVideoAd == null) {
            rewardedVideoAd = RewardedVideoAd(context, placementId)
        }

        return try {
            val ad = rewardedVideoAd ?: return false
            if (!ad.isAdLoaded) {
                val loadConfig = ad.buildLoadAdConfig()
                    .withAdListener(this)
                    .build()
                ad.loadAd(loadConfig)
            }
            true
        } catch (e: Exception) {
            Log.e("RewardedVideoAdError", e.message ?: "Unknown error")
            false
        }
    }

    private fun showAd(args: HashMap<String, Any>): Boolean {
        val delay = (args["delay"] as? Int) ?: 0
        val ad = rewardedVideoAd ?: return false

        if (!ad.isAdLoaded || ad.isAdInvalidated) return false

        val showRunnable = Runnable {
            if (ad.isAdLoaded && !ad.isAdInvalidated) {
                val showConfig = ad.buildShowAdConfig().build()
                ad.show(showConfig)
            }
        }

        if (delay <= 0) {
            showRunnable.run()
        } else {
            delayHandler.postDelayed(showRunnable, delay.toLong())
        }

        return true
    }

    private fun destroyAd(): Boolean {
        rewardedVideoAd?.destroy()
        rewardedVideoAd = null
        return true
    }

    // -------------------------
    // RewardedVideoAdListener
    // -------------------------

    override fun onError(ad: Ad, adError: AdError) {
        val args = hashMapOf<String, Any>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated,
            "error_code" to adError.errorCode,
            "error_message" to adError.errorMessage
        )
        channel.invokeMethod(FacebookConstants.ERROR_METHOD, args)
    }

    override fun onAdLoaded(ad: Ad) {
        val args = hashMapOf<String, Any>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.LOADED_METHOD, args)
    }

    override fun onAdClicked(ad: Ad) {
        val args = hashMapOf<String, Any>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.CLICKED_METHOD, args)
    }

    override fun onLoggingImpression(ad: Ad) {
        val args = hashMapOf<String, Any>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.LOGGING_IMPRESSION_METHOD, args)
    }

    override fun onRewardedVideoCompleted() {
        channel.invokeMethod(FacebookConstants.REWARDED_VIDEO_COMPLETE_METHOD, true)
    }

    override fun onRewardedVideoClosed() {
        channel.invokeMethod(FacebookConstants.REWARDED_VIDEO_CLOSED_METHOD, true)
    }
}
