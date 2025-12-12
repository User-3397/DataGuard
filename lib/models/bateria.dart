import 'package:flutter/services.dart';

class Battery {
  static const _channel = MethodChannel('samples.flutter.dev/battery');

  Future<int> getBatteryLevel() async {
    try {
      final nivel = await _channel.invokeMethod<int>('getBatteryLevel');
      return nivel ?? 0;
    } on PlatformException catch (e) {
      print("Erro ao obter bateria: ${e.message}");
    }

    return 0;
  }

  // Escuta mudanças contínuas (stream)
  static const EventChannel _eventChannel =
      EventChannel('samples.flutter.dev/batteryStream');

  static Stream<int> batteryLevelStream() {
    return _eventChannel.receiveBroadcastStream().map((event) => event as int);
  }
}
