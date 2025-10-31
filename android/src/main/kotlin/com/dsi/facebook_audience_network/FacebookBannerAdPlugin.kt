package com.dsi.facebook_audience_network

import android.content.Context
import android.view.View
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

@Suppress("UNCHECKED_CAST")
class FacebookBannerAdPlugin(private val messenger: BinaryMessenger) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        return FacebookBannerAdView(context, id, args as? HashMap<String, Any?> ?: hashMapOf(), messenger)
    }
}

class FacebookBannerAdView(
    private val context: Context,
    private val id: Int,
    private val args: HashMap<String, Any?>,
    private val messenger: BinaryMessenger
) : PlatformView, AdListener {

    private val adView: AdView
    private val channel: MethodChannel = MethodChannel(
        messenger,
        "${FacebookConstants.BANNER_AD_CHANNEL}_$id"
    )

    init {
        val placementId = args["id"] as? String ?: ""
        adView = AdView(context, placementId, getBannerSize(args))
        val loadConfig = adView.buildLoadAdConfig()
            .withAdListener(this)
            .build()
        adView.loadAd(loadConfig)
    }

    private fun getBannerSize(args: HashMap<String, Any?>): AdSize {
        val height = (args["height"] as? Int) ?: 50
        return when {
            height >= 250 -> AdSize.RECTANGLE_HEIGHT_250
            height >= 90 -> AdSize.BANNER_HEIGHT_90
            else -> AdSize.BANNER_HEIGHT_50
        }
    }

    override fun getView(): View = adView

    override fun dispose() {
        // Uncomment if you need to manually destroy the banner
        // adView.destroy()
    }

    override fun onError(ad: Ad, adError: AdError) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated,
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
