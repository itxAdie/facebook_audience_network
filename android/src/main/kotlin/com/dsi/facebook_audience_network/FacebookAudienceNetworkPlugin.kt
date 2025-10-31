package com.dsi.facebook_audience_network

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

@Suppress("UNCHECKED_CAST")
class FacebookAudienceNetworkPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel
    private lateinit var interstitialAdChannel: MethodChannel
    private lateinit var rewardedAdChannel: MethodChannel
    private var activity: Activity? = null
    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val messenger = flutterPluginBinding.binaryMessenger
        context = flutterPluginBinding.applicationContext

        channel = MethodChannel(messenger, FacebookConstants.MAIN_CHANNEL)
        channel.setMethodCallHandler(this)

        // Interstitial Ad channel
        interstitialAdChannel = MethodChannel(messenger, FacebookConstants.INTERSTITIAL_AD_CHANNEL)
        interstitialAdChannel.setMethodCallHandler(
            FacebookInterstitialAdPlugin(context, interstitialAdChannel)
        )

        // Rewarded video Ad channel
        rewardedAdChannel = MethodChannel(messenger, FacebookConstants.REWARDED_VIDEO_CHANNEL)
        rewardedAdChannel.setMethodCallHandler(
            FacebookRewardedVideoAdPlugin(context, rewardedAdChannel)
        )

        // Register banner and native factories
        flutterPluginBinding.platformViewRegistry.registerViewFactory(
            FacebookConstants.BANNER_AD_CHANNEL,
            FacebookBannerAdPlugin(messenger)
        )
        flutterPluginBinding.platformViewRegistry.registerViewFactory(
            FacebookConstants.NATIVE_AD_CHANNEL,
            FacebookNativeAdPlugin(messenger)
        )
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            FacebookConstants.INIT_METHOD -> init(call.arguments as HashMap<String, Any?>, result)
            else -> result.notImplemented()
        }
    }

    private fun init(initValues: HashMap<String, Any?>, result: Result) {
        val testingId = initValues["testingId"] as? String
        val testMode = initValues["testMode"] as? Boolean ?: false

        if (testingId != null) {
            AdSettings.addTestDevice(testingId)
        }
        if (testMode) {
            AdSettings.setTestMode(true)
        }

        val appContext = activity?.applicationContext ?: context
        AudienceNetworkAds.buildInitSettings(appContext)
            .withInitListener { initResult ->
                result.success(initResult.isSuccess)
            }
            .initialize()
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        interstitialAdChannel.setMethodCallHandler(null)
        rewardedAdChannel.setMethodCallHandler(null)
    }

    // ActivityAware methods
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromActivityForConfigChanges() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }
}
