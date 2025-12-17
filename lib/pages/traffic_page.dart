import 'dart:async';
import 'package:flutter/material.dart';
import 'package:user_3301/traffic_channel.dart';
import 'package:user_3301/vpn_channel.dart'; // canal de permissão VPN

class TrafficPage extends StatefulWidget {
  const TrafficPage({Key? key}) : super(key: key);

  @override
  State<TrafficPage> createState() => _TrafficPageState();
}

class _TrafficPageState extends State<TrafficPage> {
  bool _capturando = false;
  final List<String> _trafego = [];
  Stream<String>? _stream;
  StreamSubscription<String>? _subscription;

  void _startCapture() {
    setState(() {
      _capturando = true;
      _trafego.clear();
    });

    _stream = TrafficChannel.trafficStream();
    _subscription = _stream!.listen((packet) {
      setState(() {
        _trafego.insert(0, packet);
        if (_trafego.length > 70) {
          _trafego.removeLast();
        }
      });
    }, onError: (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text("Erro ao capturar tráfego: $e",
              style: const TextStyle(color: Colors.white)),
          backgroundColor: Color(0xff460500),
        ),
      );
    });
  }

  void _stopCapture() {
    _subscription?.cancel();
    setState(() {
      _capturando = false;
    });
  }

  void _sendReport() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text("Relatório enviado (futuro Supabase)"),
        backgroundColor: Colors.blue,
      ),
    );
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Tráfego de Rede")),
      body: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              ElevatedButton(
                onPressed: _capturando ? null : _startCapture,
                child: const Text("Start"),
              ),
              ElevatedButton(
                onPressed: _capturando ? _stopCapture : null,
                child: const Text("Stop"),
              ),
              ElevatedButton(
                onPressed: _sendReport,
                child: const Text("Send"),
              ),
            ],
          ),
          const Divider(),
          Column(children: [
            ElevatedButton(
              onPressed: () async {
                final granted = await VpnChannel.requestVpnPermission();
                if (granted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text("Permissão de VPN concedida")),
                  );

                  // iniciando a captura
                  _startCapture();
                } else {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                        content: Text("Permissão de VPN pendente ou negada")),
                  );
                }
              },
              child: const Text("Pedir Permissão VPN"),
            ),
          ]),
          const Divider(),
          Expanded(
              child: _capturando
                  ? Center(
                      child: CircularProgressIndicator(
                        //value: 0.7, // 70% progress
                        backgroundColor: Color(0xff007ad7),
                        //valueColor: AlwaysStoppedAnimation<Color>(Colors.blue),
                        strokeWidth: 5.0,
                      ),
                    )
                  : _trafego.isEmpty
                      ? const Text("Sem captura.")
                      : ListView.builder(
                          itemCount: _trafego.length,
                          itemBuilder: (context, index) {
                            return ListTile(
                              dense: true,
                              title: Text(_trafego[index]),
                            );
                          },
                        )),
        ],
      ),
    );
  }
}
