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

/*

- Cada pacote interceptado pelo VpnService é parseado pelo PacketParser.
- Extraímos IP origem/destino, portas, protocolo, TTL, tamanho.
- Enviamos como string para o Flutter via EventChannel.
- No Flutter, você pode ouvir TrafficChannel.trafficStream() e atualizar sua UI (TrafficPage) em tempo real.
 
 */