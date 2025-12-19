package com.example.user_3301

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.net.TrafficStats
import android.os.BatteryManager

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
//import kotlin.concurrent.thread
//import kotlinx.coroutines.CompletableDeferred

class MainActivity : FlutterActivity(){
    private val CHANNEL = "user_3301.dev/uso_rede" // uso de rede (TrafficStats)
    private val TRAFFIC_CHANNEL = "user_3301.dev/traffic" // stream de tráfego
    private val VPN_CHANNEL = "user_3301.dev/vpn" // permissão VPN
    private var service: TrafficVpnService? = null // referência ao serviço de tráfego
    //private var vpnPermissionCompleter: CompletableDeferred<Boolean>? = null
    // Guardar o resultado pendente da chamada Flutter:
    private var pendingResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // Canal para pedir permissão de VPN
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, VPN_CHANNEL) .setMethodCallHandler {
            call, result -> 
                if (call.method == "requestVpnPermission") {
                    val intent = VpnService.prepare(this) 
                    if (intent != null) { 
                        // ainda não tem permissão → abre tela de confirmação 
                        pendingResult =result
                        //vpnPermissionCompleter = CompletableDeferred()
                        startActivityForResult(intent, 100) 
                        // Aguardar o resultado assincronamente
                        //thread {
                        //    try {
                        //        val granted = vpnPermissionCompleter?.await() ?: false
                        //        result.success(granted)
                        //    } catch (e: Exception) {
                        //        result.error("VPN_PERMISSION_ERROR", e.message, null)
                        //    }
                        //}
                    } else { // já tem permissão 
                        result.success(true) 
                    } 
                } else { 
                    result.notImplemented() 
                } 
            }

        // Channel para uso de dados (TrafficStats):
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

        // private fun pedirPermissaoVpn() {
        //     val intent = VpnService.prepare(this)
        //     if (intent != null) {
        //         // ainda não tem permissão → abre tela de confirmação
        //         startActivityForResult(intent, 100) 
        //     } else {
        //         // já tem permissão → pode iniciar direto
        //         iniciarVpnService()
        //     }
        // }
        
        // private fun iniciarVpnService() {
        //     val intent = Intent(this, TrafficVpnService::class.java)
        //     startService(intent)
        // }

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

        // Stream contínuo de bateria: 
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

        // Stream contínuo de tráfego (via VpnService)
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, TRAFFIC_CHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                //private var service: TrafficVpnService? = null

                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    service = TrafficVpnService()
                    service?.setEventSink(events)
                    service?.onCreate()
                }

                override fun onCancel(arguments: Any?) {
                    service?.onDestroy()
                    service = null
                }
            })
        

    }

    // resultado da tela de permissao VPN
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val granted = (resultCode == RESULT_OK)
            pendingResult?.success(granted)
            pendingResult = null
        }
    }
}

