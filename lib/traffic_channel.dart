import 'package:flutter/services.dart';

class TrafficChannel {
  static const EventChannel _eventChannel =
      EventChannel('user_3301.dev/traffic');

  static Stream<String> trafficStream() {
    return _eventChannel
        .receiveBroadcastStream()
        .map((event) => event as String);
  }
}

class VpnChannel {
  static const MethodChannel _channel = MethodChannel('user_3301.dev/vpn');
  static Future<bool> requestVpnPermission() async {
    final granted = await _channel.invokeMethod<bool>('requestVpnPermission');
    return granted ?? false;
  }
}
/*

- Cada pacote interceptado pelo VpnService é parseado pelo PacketParser.
- Extraímos IP origem/destino, portas, protocolo, TTL, tamanho.
- Enviamos como string para o Flutter via EventChannel.
- No Flutter, você pode ouvir TrafficChannel.trafficStream() e atualizar sua UI (TrafficPage) em tempo real.
 
 */
