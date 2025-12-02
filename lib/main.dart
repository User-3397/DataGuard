import 'package:flutter/material.dart';
import 'dataUsagePage.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      // Application name
      title: 'DataGuard',

      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: Color(0xff272727),
        appBarTheme: AppBarTheme(
          backgroundColor: Colors.grey[900],
          foregroundColor: Colors.white,
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.grey[800],
            foregroundColor: Colors.white,
          ),
        ),
        textTheme: ThemeData.dark().textTheme.apply(
              bodyColor: Colors.white, // texto branco
              displayColor: Colors.white,
            ),

        // useMaterial3: false,
        //primarySwatch: Colors.amber,
      ),
      // A widget which will be started on application startup
      home: const MyHomePage(title: '3F4D221AB'),
    );
  }
}

class MyHomePage extends StatelessWidget {
  final String title;
  const MyHomePage({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Container(
        child: DecoratedBox(
          decoration: const BoxDecoration(
            image: DecorationImage(
              image: AssetImage("lib/assets/images/digital_03.jpg"),
              fit: BoxFit.cover,
            ),
          ),
          child: Center(
            child: ElevatedButton(
              child: const Text('Ver uso de dados'),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => DataUsageScreen()),
                );
              },
            ),
          ),
        ),
      ),
    );
  }
}
