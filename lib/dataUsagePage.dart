import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/services.dart';

class DataUsageScreen extends StatelessWidget {
  final List<double> uploadData = [10, 20, 15, 30, 25]; // MB
  final List<double> downloadData = [50, 40, 60, 70, 65]; // MB

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('DataGuard')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: LineChart(
          LineChartData(
            titlesData: FlTitlesData(show: true),
            lineBarsData: [
              LineChartBarData(
                spots: uploadData
                    .asMap()
                    .entries
                    .map((e) => FlSpot(e.key.toDouble(), e.value))
                    .toList(),
                isCurved: true,
                //colors: [Colors.red],
                color: Colors.red,
                barWidth: 2,
                dotData: FlDotData(show: false),
              ),
              LineChartBarData(
                spots: downloadData
                    .asMap()
                    .entries
                    .map((e) => FlSpot(e.key.toDouble(), e.value))
                    .toList(),
                isCurved: true,
                //colors: [Colors.blue],
                color: Colors.blue,
                barWidth: 2,
                dotData: FlDotData(show: false),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void verificarPico(List<double> dados) {
    final media = dados.reduce((a, b) => a + b) / dados.length;
    final pico = dados.any((d) => d > media * 2);
    if (pico) {
      // Exibir alerta
      print('<!> Pico de atividade detectado!');
    }
  }

  Future<void> obterUsoDeDados() async {
    const channel = MethodChannel('io./user_3301/uso_dados');
    try {
      final resultado = await channel.invokeMethod('obterUsoDeDados');
      print('Dados de uso: $resultado');
    } catch (e) {
      print('Erro: $e');
    }
  }
}
