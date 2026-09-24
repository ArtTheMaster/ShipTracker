import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/logistics_service.dart';
import '../models/enums.dart';

class DashboardScreen extends StatelessWidget {
  final LogisticsService service;
  final Function(int) onNavigateTab;

  const DashboardScreen({
    super.key,
    required this.service,
    required this.onNavigateTab,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final shipments = service.shipments;
    final totalParcels = shipments.length;
    final preparedCount = shipments.where((s) => s.status == ShipmentStatus.prepared).length;
    final dispatchedCount = shipments.where((s) => s.status == ShipmentStatus.dispatched).length;
    final deliveredCount = shipments.where((s) => s.status == ShipmentStatus.delivered).length;
    final returnedCount = shipments.where((s) => s.status == ShipmentStatus.returned).length;
    final totalValue = shipments.fold<double>(0.0, (sum, s) => sum + s.declaredValue);
    final returnRate = totalParcels > 0 ? (returnedCount / totalParcels) * 100 : 0.0;
    final currencyFormat = NumberFormat.currency(symbol: '₱', decimalDigits: 2);
    final timeFormat = DateFormat('MMM dd, hh:mm a');

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('GJandAsher Outbound Logistics', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            Text(
              'User: ${service.currentUser.fullName} (${service.currentUser.role.displayName})',
              style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant),
            ),
          ],
        ),
        actions: [
          IconButton(
            tooltip: 'Sync Center',
            icon: Icon(
              service.isOnline ? Icons.cloud_done : Icons.cloud_off,
              color: service.isOnline ? const Color(0xFF059669) : const Color(0xFFDC2626),
            ),
            onPressed: () => onNavigateTab(6), // Navigate to Sync
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16.0),
        children: [
          // Banner
          Card(
            color: theme.colorScheme.primaryContainer,
            elevation: 0,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: theme.colorScheme.primary,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(Icons.local_shipping, color: Colors.white, size: 28),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Warehouse Hub Operational',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 15,
                            color: theme.colorScheme.onPrimaryContainer,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Cross-platform dispatch & RTS tracking active.',
                          style: TextStyle(
                            fontSize: 12,
                            color: theme.colorScheme.onPrimaryContainer.withOpacity(0.85),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // KPI Grid
          Text('Key Logistics Metrics', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(child: _buildMetricTile(context, 'Total Outbound', '$totalParcels', Icons.inventory_2, theme.colorScheme.primary)),
              const SizedBox(width: 10),
              Expanded(child: _buildMetricTile(context, 'Packed / Ready', '$preparedCount', Icons.check_circle_outline, const Color(0xFF0D9488))),
            ],
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(child: _buildMetricTile(context, 'Dispatched', '$dispatchedCount', Icons.local_shipping_outlined, const Color(0xFF2563EB))),
              const SizedBox(width: 10),
              Expanded(child: _buildMetricTile(context, 'RTS Return Rate', '${returnRate.toStringAsFixed(1)}%', Icons.assignment_return, const Color(0xFFDC2626))),
            ],
          ),
          const SizedBox(height: 10),

          // Financial Scope (RBAC check)
          if (service.currentUser.role.canViewFinancialMetrics)
            Card(
              elevation: 0,
              color: theme.colorScheme.surfaceVariant,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Total Declared Value', style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant)),
                        const SizedBox(height: 4),
                        Text(
                          currencyFormat.format(totalValue),
                          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
                        ),
                      ],
                    ),
                    Chip(
                      label: const Text('Admin Only', style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold)),
                      backgroundColor: theme.colorScheme.primary.withOpacity(0.12),
                    ),
                  ],
                ),
              ),
            ),
          const SizedBox(height: 16),

          // Quick Action Buttons
          Row(
            children: [
              Expanded(
                child: FilledButton.icon(
                  onPressed: () => onNavigateTab(1), // Scanner
                  icon: const Icon(Icons.qr_code_scanner),
                  label: const Text('Scan Parcel'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () => onNavigateTab(3), // Dispatch
                  icon: const Icon(Icons.send_and_archive),
                  label: const Text('Dispatch Batch'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Recent Activity Log
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Audit & Operations Trail', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
              Text('${service.logs.length} events', style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant)),
            ],
          ),
          const SizedBox(height: 10),
          if (service.logs.isEmpty)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 20),
              child: Center(child: Text('No operational logs recorded yet.')),
            )
          else
            ...service.logs.take(5).map((log) => Card(
              elevation: 0,
              margin: const EdgeInsets.only(bottom: 8),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
                side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
              ),
              child: ListTile(
                dense: true,
                leading: CircleAvatar(
                  radius: 16,
                  backgroundColor: theme.colorScheme.primary.withOpacity(0.1),
                  child: Icon(Icons.history, size: 16, color: theme.colorScheme.primary),
                ),
                title: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(log.action, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                    Text(timeFormat.format(log.timestamp), style: TextStyle(fontSize: 10, color: theme.colorScheme.onSurfaceVariant)),
                  ],
                ),
                subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const SizedBox(height: 2),
                    Text(log.details, style: const TextStyle(fontSize: 11)),
                    Text('By ${log.performedBy}', style: TextStyle(fontSize: 10, color: theme.colorScheme.onSurfaceVariant)),
                  ],
                ),
              ),
            )),
        ],
      ),
    );
  }

  Widget _buildMetricTile(BuildContext context, String title, String value, IconData icon, Color color) {
    final theme = Theme.of(context);
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(14.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(title, style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant)),
                Icon(icon, size: 18, color: color),
              ],
            ),
            const SizedBox(height: 8),
            Text(value, style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: color)),
          ],
        ),
      ),
    );
  }
}
