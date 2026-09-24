import 'package:flutter/material.dart';
import '../services/logistics_service.dart';
import 'dashboard_screen.dart';
import 'scanner_screen.dart';
import 'shipments_screen.dart';
import 'dispatch_screen.dart';
import 'returns_screen.dart';
import 'reports_screen.dart';
import 'sync_screen.dart';
import 'account_screen.dart';

class MainNavigationScreen extends StatefulWidget {
  final LogisticsService service;

  const MainNavigationScreen({super.key, required this.service});

  @override
  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends State<MainNavigationScreen> {
  int _currentIndex = 0;

  void _onSelectTab(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    final screens = [
      DashboardScreen(service: widget.service, onNavigateTab: _onSelectTab),
      ScannerScreen(service: widget.service),
      ShipmentsScreen(service: widget.service),
      DispatchScreen(service: widget.service),
      ReturnsScreen(service: widget.service),
      ReportsScreen(service: widget.service),
      SyncScreen(service: widget.service),
      AccountScreen(service: widget.service),
    ];

    return Scaffold(
      body: screens[_currentIndex],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: _onSelectTab,
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        destinations: const [
          NavigationDestination(icon: Icon(Icons.dashboard_outlined), selectedIcon: Icon(Icons.dashboard), label: 'Dashboard'),
          NavigationDestination(icon: Icon(Icons.qr_code_scanner_outlined), selectedIcon: Icon(Icons.qr_code_scanner), label: 'Scan'),
          NavigationDestination(icon: Icon(Icons.inventory_2_outlined), selectedIcon: Icon(Icons.inventory_2), label: 'Parcels'),
          NavigationDestination(icon: Icon(Icons.local_shipping_outlined), selectedIcon: Icon(Icons.local_shipping), label: 'Dispatch'),
          NavigationDestination(icon: Icon(Icons.assignment_return_outlined), selectedIcon: Icon(Icons.assignment_return), label: 'Returns'),
          NavigationDestination(icon: Icon(Icons.bar_chart_outlined), selectedIcon: Icon(Icons.bar_chart), label: 'Reports'),
          NavigationDestination(icon: Icon(Icons.sync_outlined), selectedIcon: Icon(Icons.sync), label: 'Sync'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Account'),
        ],
      ),
    );
  }
}
