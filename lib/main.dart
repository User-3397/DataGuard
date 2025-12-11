import 'dart:html';

import 'package:flutter/material.dart';
import 'package:user_3301/pages/homepage.dart';
import 'package:user_3301/pages/battery_page.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
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
      home: const HomePage(),
    );
  }
}
