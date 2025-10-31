package com.dsi.facebook_audience_network

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import com.facebook.ads.*
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class FacebookNativeAdPlugin(
    private val messenger: BinaryMessenger
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        return FacebookNativeAdView(context, id, args as HashMap<*, *>, messenger)
    }
}

class FacebookNativeAdView(
    private val context: Context,
    id: Int,
    private val args: HashMap<*, *>,
    messenger: BinaryMessenger
) : PlatformView, NativeAdListener {

    private val adView: LinearLayout = LinearLayout(context)
    private val channel = MethodChannel(messenger, "${FacebookConstants.NATIVE_AD_CHANNEL}_$id")

    private var nativeAd: NativeAd? = null
    private var bannerAd: NativeBannerAd? = null

    init {
        val isBannerAd = args["banner_ad"] as? Boolean ?: false
        val placementId = args["id"] as? String ?: return

        if (isBannerAd) {
            bannerAd = NativeBannerAd(context, placementId)
            val loadConfig = bannerAd!!.buildLoadAdConfig()
                .withAdListener(this)
                .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                .build()
            bannerAd!!.loadAd(loadConfig)
        } else {
            nativeAd = NativeAd(context, placementId)
            val loadConfig = nativeAd!!.buildLoadAdConfig()
                .withAdListener(this)
                .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                .build()
            nativeAd!!.loadAd(loadConfig)
        }
    }

    private fun getViewAttributes(): NativeAdViewAttributes {
        val viewAttributes = NativeAdViewAttributes(context)

        (args["bg_color"] as? String)?.let {
            viewAttributes.setBackgroundColor(Color.parseColor(it))
        }
        (args["title_color"] as? String)?.let {
            viewAttributes.setTitleTextColor(Color.parseColor(it))
        }
        (args["desc_color"] as? String)?.let {
            viewAttributes.setDescriptionTextColor(Color.parseColor(it))
        }
        (args["button_color"] as? String)?.let {
            viewAttributes.setButtonColor(Color.parseColor(it))
        }
        (args["button_title_color"] as? String)?.let {
            viewAttributes.setButtonTextColor(Color.parseColor(it))
        }
        (args["button_border_color"] as? String)?.let {
            viewAttributes.setButtonBorderColor(Color.parseColor(it))
        }

        return viewAttributes
    }

    private fun getBannerSize(): NativeBannerAdView.Type {
        val height = (args["height"] as? Int) ?: 120
        return when (height) {
            50 -> NativeBannerAdView.Type.HEIGHT_50
            100 -> NativeBannerAdView.Type.HEIGHT_100
            120 -> NativeBannerAdView.Type.HEIGHT_120
            else -> NativeBannerAdView.Type.HEIGHT_120
        }
    }

    override fun getView(): View = adView

    override fun dispose() {
        // Clean-up handled automatically by SDK
    }

    override fun onError(ad: Ad?, adError: AdError) {
        val data = hashMapOf<String, Any?>(
            "placement_id" to (ad?.placementId ?: ""),
            "invalidated" to (ad?.isAdInvalidated ?: false),
            "error_code" to adError.errorCode,
            "error_message" to adError.errorMessage
        )
        channel.invokeMethod(FacebookConstants.ERROR_METHOD, data)
    }

    override fun onAdLoaded(ad: Ad) {
        val data = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.LOAD_SUCCESS_METHOD, data)

        // Small delay to ensure proper rendering on Flutter side
        adView.postDelayed({ showNativeAd() }, 200)
    }

    private fun showNativeAd() {
        adView.removeAllViews()

        val viewAttributes = getViewAttributes()
        val isBannerAd = args["banner_ad"] as? Boolean ?: false

        if (isBannerAd) {
            bannerAd?.let {
                adView.addView(
                    NativeBannerAdView.render(context, it, getBannerSize(), viewAttributes)
                )
            }
        } else {
            nativeAd?.let {
                adView.addView(
                    NativeAdView.render(context, it, viewAttributes)
                )
            }
        }

        channel.invokeMethod(FacebookConstants.LOADED_METHOD, args)
    }

    override fun onAdClicked(ad: Ad) {
        val data = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.CLICKED_METHOD, data)
    }

    override fun onLoggingImpression(ad: Ad) {
        val data = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.LOGGING_IMPRESSION_METHOD, data)
    }

    override fun onMediaDownloaded(ad: Ad) {
        val data = hashMapOf<String, Any?>(
            "placement_id" to ad.placementId,
            "invalidated" to ad.isAdInvalidated
        )
        channel.invokeMethod(FacebookConstants.MEDIA_DOWNLOADED_METHOD, data)
    }
}
