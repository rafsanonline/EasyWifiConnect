
import 'dart:async';

import 'package:flutter/services.dart';

class EasyWifiConnect {
  static const MethodChannel _channel =
      const MethodChannel('easy_wifi_connect');

  static Future<String?> connectWifi(String ssid, String password) async {
    Map<String,dynamic> args = <String, dynamic>{};
    args.putIfAbsent("ssid", () => ssid);
    args.putIfAbsent("pass", () => password);
    await _channel.invokeMethod('wificonnect', args);
  }
}
