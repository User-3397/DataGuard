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
            if (nivelBat <= 17){
                eventSink?.success("Captura encerrada: $nivelBat% de bateria.")
                stopSelf()
                break
            }
            
            buffer.clear()
            val length = inputStream.read(buffer.array())
            if (length > 0) {
                buffer.limit(length)
                if (length >= 20) { // Mínimo para header IP
                    val info = PacketParser.parsePacket(buffer)
                    eventSink?.success(info)
                } else {
                    eventSink?.success("Pacote ignorado: tamanho $length < 20")
                }
            } else if (length == -1) {
                // EOF, talvez reconectar ou parar
                break
            }
            // Pequena pausa para não sobrecarregar
            Thread.sleep(10)
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

        if (buffer.remaining() < 20) return "Pacote muito pequeno"

        val versionAndIhl = buffer.get().toInt()
        val version = (versionAndIhl shr 4) and 0xF
        val ihl = (versionAndIhl and 0xF) * 4

        if (version == 4) {
            return parseIPv4(buffer, ihl)
        } else if (version == 6) {
            return parseIPv6(buffer)
        } else {
            return "Versão IP não suportada: $version"
        }
    }

    private fun parseIPv4(buffer: ByteBuffer, ihl: Int): String {
        if (buffer.remaining() < ihl) return "Header IPv4 incompleto"

        buffer.get() // TOS
        val totalLength = buffer.short.toInt() and 0xFFFF
        buffer.short // identificação
        buffer.short // flags + fragment offset
        val ttl = buffer.get().toInt() and 0xFF
        val protocol = buffer.get().toInt() and 0xFF
        buffer.short // checksum

        val srcBytes = ByteArray(4)
        buffer.get(srcBytes)
        val srcIp = try {
            InetAddress.getByAddress(srcBytes).hostAddress
        } catch (e: Exception) {
            "IP inválido"
        }

        val dstBytes = ByteArray(4)
        buffer.get(dstBytes)
        val dstIp = try {
            InetAddress.getByAddress(dstBytes).hostAddress
        } catch (e: Exception) {
            "IP inválido"
        }

        // Pular opções se houver
        val optionsLength = ihl - 20
        if (optionsLength > 0 && buffer.remaining() >= optionsLength) {
            buffer.position(buffer.position() + optionsLength)
        }

        var srcPort = -1
        var dstPort = -1
        var protoName = getProtocolName(protocol)

        if ((protocol == 6 || protocol == 17) && buffer.remaining() >= 8) { // TCP ou UDP
            srcPort = buffer.short.toInt() and 0xFFFF
            dstPort = buffer.short.toInt() and 0xFFFF
        }

        return "$protoName $srcIp:$srcPort → $dstIp:$dstPort (TTL=$ttl, Len=$totalLength)"
    }

    private fun parseIPv6(buffer: ByteBuffer): String {
        if (buffer.remaining() < 40) return "Header IPv6 incompleto"

        buffer.get() // Traffic Class (parte)
        buffer.short // Flow Label
        val payloadLength = buffer.short.toInt() and 0xFFFF
        val nextHeader = buffer.get().toInt() and 0xFF
        val hopLimit = buffer.get().toInt() and 0xFF

        val srcBytes = ByteArray(16)
        buffer.get(srcBytes)
        val srcIp = try {
            InetAddress.getByAddress(srcBytes).hostAddress
        } catch (e: Exception) {
            "IPv6 inválido"
        }

        val dstBytes = ByteArray(16)
        buffer.get(dstBytes)
        val dstIp = try {
            InetAddress.getByAddress(dstBytes).hostAddress
        } catch (e: Exception) {
            "IPv6 inválido"
        }

        var srcPort = -1
        var dstPort = -1
        var protoName = getProtocolName(nextHeader)

        if ((nextHeader == 6 || nextHeader == 17) && buffer.remaining() >= 8) { // TCP ou UDP
            srcPort = buffer.short.toInt() and 0xFFFF
            dstPort = buffer.short.toInt() and 0xFFFF
        }

        return "$protoName [$srcIp]:$srcPort → [$dstIp]:$dstPort (HopLimit=$hopLimit, PayloadLen=$payloadLength)"
    }

    private fun getProtocolName(protocol: Int): String {
        return when (protocol) {
            1 -> "ICMP"
            2 -> "IGMP"
            6 -> "TCP"
            17 -> "UDP"
            41 -> "IPv6"
            58 -> "ICMPv6"
            else -> "PROTO_$protocol"
        }
    }
}
