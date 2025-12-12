package com.example.user_3301

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import android.net.TrafficStats

class MainActivity : FlutterActivity(){
    private val CHANNEL = "io.user_3301/uso_rede"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // Channel para Mb de dados:
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            if (call.method == "obterUsoRede") {
                val uid = android.os.Process.myUid()
                val recebido = TrafficStats.getUidRxBytes(uid)
                val enviado = TrafficStats.getUidTxBytes(uid)

                val dados = mapOf(
                    "recebido" to recebido,
                    "enviado" to enviado
                )
                result.success(dados)
            } else {
                result.notImplemented()
            }
        }

        // Channel para VPN
        // MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "user_3301.dev/vpn")
        //     .setMethodCallHandler { call, result ->
        //         if (call.method == "iniciarVpn") {
        //             MyVpnService.start(this)
        //             result.success(null)
        //         } else if (call.method == "pararVpn") {
        //             MyVpnService.stop(this)
        //             result.success(null)
        //         } else {
        //             result.notImplemented()
        //         }
        //     }

        // Channel para bateria
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "user_3301.dev/battery")
            .setMethodCallHandler { call, result ->
                if (call.method == "obterNivel") {
                    val nivel = BatteryHelper.getBatteryLevel(this)
                    result.success(nivel)
                } else {
                    result.notImplemented()
                }
            }

        // Stream contínuo
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, "user_3301.dev/batteryStream").setStreamHandler(
            object : EventChannel.StreamHandler {
                private var receiver: BroadcastReceiver? = null

                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            val nivel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                            events?.success(nivel)
                        }
                    }
                    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    registerReceiver(receiver, filter)
                }

                override fun onCancel(arguments: Any?) {
                    unregisterReceiver(receiver)
                    receiver = null
                }
            }
        )
    }


}

