import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'facebook_audience_network_platform_interface.dart';

/// An implementation of [FacebookAudienceNetworkPlatform] that uses method channels.
class MethodChannelFacebookAudienceNetwork extends FacebookAudienceNetworkPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('facebook_audience_network');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
