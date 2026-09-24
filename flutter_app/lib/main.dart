import 'package:flutter/material.dart';
import 'services/logistics_service.dart';
import 'screens/main_navigation_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  final service = LogisticsService();
  runApp(ShipTrackerApp(service: service));
}

class ShipTrackerApp extends StatelessWidget {
  final LogisticsService service;

  const ShipTrackerApp({super.key, required this.service});

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: service,
      builder: (context, _) {
        return MaterialApp(
          title: 'GJandAsher Outbound Logistics',
          debugShowCheckedModeBanner: false,
          theme: ThemeData(
            useMaterial3: true,
            colorScheme: ColorScheme.fromSeed(
              seedColor: const Color(0xFF1E3A8A), // Logistics Navy
              primary: const Color(0xFF1E3A8A),
              secondary: const Color(0xFFF97316), // Logistics Orange
              surface: Colors.white,
            ),
            fontFamily: 'Roboto',
            appBarTheme: const AppBarTheme(
              elevation: 0,
              centerTitle: false,
              backgroundColor: Colors.white,
              surfaceTintColor: Colors.transparent,
            ),
          ),
          darkTheme: ThemeData(
            useMaterial3: true,
            brightness: Brightness.dark,
            colorScheme: ColorScheme.fromSeed(
              brightness: Brightness.dark,
              seedColor: const Color(0xFF38BDF8),
              primary: const Color(0xFF38BDF8),
              secondary: const Color(0xFFFB923C),
            ),
          ),
          themeMode: ThemeMode.system,
          home: MainNavigationScreen(service: service),
        );
      },
    );
  }
}
