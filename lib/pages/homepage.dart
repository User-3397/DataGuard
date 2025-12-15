import 'package:flutter/material.dart';
import 'package:user_3301/pages/traffic_page.dart';
import 'package:user_3301/models/bateria.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final Battery _battery = Battery();
  int _nivelBateria = 0;
  int _bytesEnviados = 0;
  int _bytesRecebidos = 0;

  @override
  void initState() {
    super.initState();

    // leitura inicial
    _getBatteryLevel();

    // escuta contínua
    Battery.batteryLevelStream().listen((nivel) {
      setState(() => _nivelBateria = nivel);
    }, onError: (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text("<Erro> (batteryLevelStream): $e"),
          backgroundColor: Colors.red,
        ),
      );
    });
  }

  //Future<void> getData() async {
  //final supabase = Supabase.instance.client;
  //final data = await supabase.from('tabela').select();
  //print(data);
  //}

  Future<void> _getBatteryLevel() async {
    try {
      final nivel = await _battery.getBatteryLevel();
      setState(() => _nivelBateria = nivel);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
            backgroundColor: const Color(0xff490500),
            content: Text("<Erro> (_getBatteryLevel): $e",
                style: const TextStyle(color: Colors.white)),
            action: SnackBarAction(
              label: "Ok",
              textColor: Colors.yellow,
              onPressed: () {
                // Exemplo: apenas fechar o snackbar ou registrar log
                ScaffoldMessenger.of(context).hideCurrentSnackBar();
              },
            )),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Pagina Principal"),
        actions: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Text(
                "B: $_nivelBateria%"), // | ${_bytesEnviados ~/ 1024}KB | ${_bytesRecebidos ~/ 1024}KB"),
          ),
        ],
      ),
      body: Container(
        child: DecoratedBox(
          decoration: const BoxDecoration(
            image: DecorationImage(
              image: AssetImage("lib/assets/images/digital_03.jpg"),
              fit: BoxFit.cover,
            ),
          ),
          child: Center(
              child: Column(children: [
            ElevatedButton(
              child: const Text('Ver uso de dados'),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const TrafficPage()),
                );
              },
            ),
            const SizedBox(height: 30),
            //ElevatedButton(
            //  child: Text('Ver uso de dados'),
            //  onPressed: () {
            // Navigator.push(
            //   context,
            //   MaterialPageRoute(builder: (context) => BatteryPage()),
            // );
            //  },
            //),
          ])),
        ),
      ),
    );
  }
}
