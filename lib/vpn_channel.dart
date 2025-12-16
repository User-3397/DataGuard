import 'package:flutter/services.dart';

/// Classe que encapsula o canal de comunicação com o Android
/// para pedir permissão de VPN em tempo de execução.
class VpnChannel {
  // Nome do canal deve ser o mesmo usado no MainActivity.kt
  static const MethodChannel _channel = MethodChannel('user_3301.dev/vpn');

  /// Pede permissão de VPN ao Android.
  /// Retorna true se já tem permissão ou se o usuário aceitou,
  /// false se ainda está pendente ou foi negado.
  static Future<bool> requestVpnPermission() async {
    try {
      final granted = await _channel.invokeMethod<bool>('requestVpnPermission');
      return granted ?? false;
    } on PlatformException catch (e) {
      // Tratamento de erro caso o canal falhe
      print("Erro ao pedir permissão VPN: ${e.message}");
      return false;
    }
  }
}
