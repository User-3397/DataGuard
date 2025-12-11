import 'package:flutter/services.dart';

class BatteryChannel {
  static const _channel = MethodChannel('io.user_3301/bateria');

  static Future<int> obterNivel() async {
    final nivel = await _channel.invokeMethod('obterNivel');
    return nivel;
  }
}
