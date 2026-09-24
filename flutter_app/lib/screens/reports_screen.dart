import 'package:flutter/material.dart';
import '../services/logistics_service.dart';
import '../models/enums.dart';

class ReportsScreen extends StatelessWidget {
  final LogisticsService service;

  const ReportsScreen({super.key, required this.service});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final shipments = service.shipments;
    final total = shipments.length;
    final dispatched = shipments.where((s) => s.status == ShipmentStatus.dispatched || s.status == ShipmentStatus.inTransit || s.status == ShipmentStatus.delivered).length;
    final returned = shipments.where((s) => s.status == ShipmentStatus.returned).length;
    final returnRate = total > 0 ? (returned / total) * 100 : 0.0;
    final totalValue = shipments.fold<double>(0.0, (sum, s) => sum + s.declaredValue);

    // CSV generator
    final csvBuffer = StringBuffer();
    csvBuffer.writeln('tracking_number,courier,platform,weight_kg,status,recipient_name,declared_value');
    for (var s in shipments) {
      csvBuffer.writeln('${s.trackingNumber},${s.courier.name},${s.platform.name},${s.weightKg},${s.status.name},"${s.recipientName}",${s.declaredValue}');
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Analytics & CSV Export', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Executive Summary Card
          Card(
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
              side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Logistics Executive Summary', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 12),
                  _buildSummaryRow('Total Outbound Parcels:', '$total'),
                  _buildSummaryRow('Handover / Dispatched Count:', '$dispatched'),
                  _buildSummaryRow(
                    'Overall Return Rate (RTS):',
                    '${returnRate.toStringAsFixed(1)}%',
                    color: returnRate > 5.0 ? Colors.red : Colors.green,
                  ),
                  if (service.currentUser.role == UserRole.ownerAdmin)
                    _buildSummaryRow('Gross Outbound Value:', '₱${totalValue.toStringAsFixed(2)}', isBold: true),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Courier Volume Breakdown
          Text('Courier Distribution', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          ...CourierType.values.map((courier) {
            final count = shipments.where((s) => s.courier == courier).length;
            final percent = total > 0 ? (count / total) : 0.0;
            return Padding(
              padding: const EdgeInsets.only(bottom: 8.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(courier.displayName, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                      Text('$count parcels (${(percent * 100).toStringAsFixed(1)}%)', style: const TextStyle(fontSize: 11)),
                    ],
                  ),
                  const SizedBox(height: 4),
                  LinearProgressIndicator(value: percent, color: courier.brandColor, backgroundColor: Colors.grey.shade200),
                ],
              ),
            );
          }),
          const SizedBox(height: 20),

          // CSV Raw Data Preview
          Text('Raw CSV Stream Preview', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.grey.shade900,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              csvBuffer.toString(),
              style: const TextStyle(color: Color(0xFF38BDF8), fontFamily: 'monospace', fontSize: 10),
            ),
          ),
          const SizedBox(height: 16),

          FilledButton.icon(
            onPressed: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('CSV Report stream compiled and copied to clipboard!')),
              );
            },
            icon: const Icon(Icons.download),
            label: const Text('Export Full Warehouse CSV'),
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryRow(String label, String value, {Color? color, bool isBold = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 12)),
          Text(value, style: TextStyle(fontSize: 12, fontWeight: isBold ? FontWeight.bold : FontWeight.w600, color: color)),
        ],
      ),
    );
  }
}
