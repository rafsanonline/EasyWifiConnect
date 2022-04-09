package com.sslwireless.easy_wifi_connect

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** EasyWifiConnectPlugin */
class EasyWifiConnectPlugin: FlutterPlugin, MethodCallHandler {

  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "easy_wifi_connect")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
    requestPermission(context)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "wificonnect") {
        connectToWifi(result,call.argument<String>("ssid"), call.argument<String>("pass"))
    } else {
      result.notImplemented()
    }
  }

  private fun connectToWifi(result: Result,ssid: String?, pass: String?) {
    if (Build.VERSION.SDK_INT >= 29) {

      val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
        .setSsid(ssid.toString())
        .setWpa2Passphrase(pass.toString())
        .build()

      val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .setNetworkSpecifier(wifiNetworkSpecifier)
//        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

      val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
          super.onAvailable(network)
          connectivityManager.bindProcessToNetwork(network)
          result.success(true)
//          Log.d("Charco", "onAvailable $network")
        }

        override fun onLost(network: Network) {
          super.onLost(network)
//          Log.d("Charco", "onLost $network")
          connectivityManager.bindProcessToNetwork(null)
        }

        override fun onUnavailable() {
          super.onUnavailable()
//          Log.d("Charco", "onUnavailable ")
          result.success(false)
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
          super.onLosing(network, maxMsToLive)
          Log.d("Charco", "onLosing bindProcessToNetwork ")

        }

      })

    } else {
      val wifiConfig = WifiConfiguration()
      wifiConfig.SSID = String.format("\"%s\"", ssid.toString())
      wifiConfig.preSharedKey = String.format("\"%s\"", pass.toString())
      //remember id
      val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
      val netId = wifiManager!!.addNetwork(wifiConfig)
      wifiManager.disconnect()
      wifiManager.enableNetwork(netId, true)
      val isConnectionSuccessful = wifiManager.reconnect()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  fun requestPermission(context: Context) {
    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(context as Activity, arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
      ), 100)
    }
  }
}
