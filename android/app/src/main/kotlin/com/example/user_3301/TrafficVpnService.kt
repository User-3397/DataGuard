package com.example.user_3301

import io.flutter.plugin.common.EventChannel
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.os.BatteryManager
import android.content.Intent 
import android.content.IntentFilter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.net.InetAddress



class TrafficVpnService : VpnService(), Runnable {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var thread: Thread? = null
    private var eventSink: EventChannel.EventSink? = null

    // Flutter injeta o EventSink para enviar dados ao Dart
    fun setEventSink(sink: EventChannel.EventSink?) {
        eventSink = sink
    }

    override fun onCreate() {
        super.onCreate()
        thread = Thread(this, "TrafficVpnThread")
        thread?.start()
    }

    override fun onDestroy() {
        thread?.interrupt()
        vpnInterface?.close()
        super.onDestroy()
    }

    override fun run() {
        // Configuração básica da VPN
        val builder = Builder()
        builder.addAddress("10.0.0.2", 32)
        builder.addRoute("0.0.0.0", 0)
        vpnInterface = builder.establish()

        val inputStream = FileInputStream(vpnInterface!!.fileDescriptor)
        val buffer = ByteBuffer.allocate(32767)

        while (!Thread.interrupted()) {
            val nivelBat =getBatteryLevel()
            if (nivelBat <= 25){
                eventSink?.success("Captura encerrada: $nivelBat% de bateria.")
                stopSelf()
                break
            }
            
            buffer.clear()
            val length = inputStream.read(buffer.array())
            if (length > 0) {
                buffer.limit(length)
                val info = PacketParser.parsePacket(buffer)
                eventSink?.success(info)
            }
        }
    }

    // Obtém nível de bateria atual
    private fun getBatteryLevel(): Int {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager 
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}

// Parser de pacotes IPv4/TCP/UDP
object PacketParser {
    fun parsePacket(buffer: ByteBuffer): String {
        buffer.position(0)

        val versionAndIhl = buffer.get().toInt()
        val version = (versionAndIhl shr 4) and 0xF
        //val ihl = (versionAndIhl and 0xF) * 4

        if (version != 4) return "Não é IPv4"

        buffer.get() // TOS
        val totalLength = buffer.short.toInt() and 0xFFFF
        buffer.short // identificação
        buffer.short // flags + fragment offset
        val ttl = buffer.get().toInt() and 0xFF
        val protocol = buffer.get().toInt() and 0xFF
        buffer.short // checksum

        val srcBytes = ByteArray(4)
        buffer.get(srcBytes)
        val srcIp = InetAddress.getByAddress(srcBytes).hostAddress

        val dstBytes = ByteArray(4)
        buffer.get(dstBytes)
        val dstIp = InetAddress.getByAddress(dstBytes).hostAddress

        var srcPort = -1
        var dstPort = -1
        var protoName = "UNKNOWN"

        if (protocol == 6 || protocol == 17) { // TCP ou UDP
            srcPort = buffer.short.toInt() and 0xFFFF
            dstPort = buffer.short.toInt() and 0xFFFF
            protoName = if (protocol == 6) "TCP" else "UDP"
        } else if (protocol == 1) {
            protoName = "ICMP"
        }

        return "$protoName $srcIp:$srcPort → $dstIp:$dstPort (TTL=$ttl, Len=$totalLength)"
    }
}
