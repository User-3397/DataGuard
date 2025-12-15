import 'package:flutter/services.dart';
import 'dart:async'; // não é obrigatorio se houver '.listen()'

class Battery {
  static const _channel = MethodChannel('user_3301.dev/battery');
//Escuta mudanças contínuas (stream)
  static const EventChannel _eventChannel =
      EventChannel('user_3301.dev/batteryStream');

  // Consulta pontual:
  Future<int> getBatteryLevel() async {
    try {
      final nivel = await _channel.invokeMethod<int>('getBatteryLevel');
      return nivel ?? 0;
    } on PlatformException catch (e) {
      print("Erro > Nivel_bateria > consulta_pontual: ${e.message}");
      return 0;
    } catch (e, s) {
      print("$e - $s");
      return 0;
    }
  }

  // C
  static Stream<int> batteryLevelStream() {
    return _eventChannel.receiveBroadcastStream().map((event) => event as int);
  }
}
