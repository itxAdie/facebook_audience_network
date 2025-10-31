package com.dsi.facebook_audience_network

object FacebookConstants {
    const val MAIN_CHANNEL = "fb.audience.network.io"
    const val BANNER_AD_CHANNEL = "$MAIN_CHANNEL/bannerAd"
    const val INTERSTITIAL_AD_CHANNEL = "$MAIN_CHANNEL/interstitialAd"
    const val NATIVE_AD_CHANNEL = "$MAIN_CHANNEL/nativeAd"
    const val REWARDED_VIDEO_CHANNEL = "$MAIN_CHANNEL/rewardedAd"

    const val INIT_METHOD = "init"
    const val SHOW_INTERSTITIAL_METHOD = "showInterstitialAd"
    const val LOAD_INTERSTITIAL_METHOD = "loadInterstitialAd"
    const val DESTROY_INTERSTITIAL_METHOD = "destroyInterstitialAd"

    const val SHOW_REWARDED_VIDEO_METHOD = "showRewardedAd"
    const val LOAD_REWARDED_VIDEO_METHOD = "loadRewardedAd"
    const val DESTROY_REWARDED_VIDEO_METHOD = "destroyRewardedAd"

    const val DISPLAYED_METHOD = "displayed"
    const val DISMISSED_METHOD = "dismissed"
    const val ERROR_METHOD = "error"
    const val LOADED_METHOD = "loaded"
    const val CLICKED_METHOD = "clicked"
    const val LOGGING_IMPRESSION_METHOD = "logging_impression"
    const val REWARDED_VIDEO_COMPLETE_METHOD = "rewarded_complete"
    const val REWARDED_VIDEO_CLOSED_METHOD = "rewarded_closed"

    const val MEDIA_DOWNLOADED_METHOD = "media_downloaded"
    const val LOAD_SUCCESS_METHOD = "load_success"
}
