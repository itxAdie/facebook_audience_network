import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'facebook_audience_network_method_channel.dart';

abstract class FacebookAudienceNetworkPlatform extends PlatformInterface {
  /// Constructs a FacebookAudienceNetworkPlatform.
  FacebookAudienceNetworkPlatform() : super(token: _token);

  static final Object _token = Object();

  static FacebookAudienceNetworkPlatform _instance =
      MethodChannelFacebookAudienceNetwork();

  /// The default instance of [FacebookAudienceNetworkPlatform] to use.
  ///
  /// Defaults to [MethodChannelFacebookAudienceNetwork].
  static FacebookAudienceNetworkPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FacebookAudienceNetworkPlatform] when
  /// they register themselves.
  static set instance(FacebookAudienceNetworkPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
