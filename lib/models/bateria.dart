import 'package:flutter/services.dart';

class Battery {
  static const platform = MethodChannel('samples.flutter.dev/battery');

  Future<void> _getBatteryLevel() async {
    try {
      final nivel = await platform.invokeMethod<int>('getBatteryLevel');
      setState(() {
        _nivelBateria = nivel ?? 0;
      });
    } on PlatformException catch (e) {
      print("Erro ao obter bateria: ${e.message}");
    }
  }
}
