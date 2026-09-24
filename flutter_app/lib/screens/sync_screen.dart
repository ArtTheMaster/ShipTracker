import 'package:flutter/material.dart';
import '../services/logistics_service.dart';
import '../models/enums.dart';

class SyncScreen extends StatelessWidget {
  final LogisticsService service;

  const SyncScreen({super.key, required this.service});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final pendingShipments = service.shipments.where((s) => s.syncStatus == SyncStatus.pendingSync).toList();
    final pendingReturns = service.returns.where((r) => r.syncStatus == SyncStatus.pendingSync).toList();
    final totalPending = pendingShipments.length + pendingReturns.length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Offline-First Sync Engine', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Network Switch Card
          Card(
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
              side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Icon(
                            service.isOnline ? Icons.wifi : Icons.wifi_off,
                            color: service.isOnline ? Colors.green : Colors.red,
                            size: 28,
                          ),
                          const SizedBox(width: 12),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                service.isOnline ? 'Network Online' : 'Warehouse Offline Mode',
                                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                              ),
                              Text(
                                service.isOnline ? 'Real-time synchronization enabled' : 'Queuing records locally in memory',
                                style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant),
                              ),
                            ],
                          ),
                        ],
                      ),
                      Switch(
                        value: service.isOnline,
                        onChanged: (val) => service.toggleNetworkConnection(),
                      ),
                    ],
                  ),
                  const Divider(height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('Pending Outbox Queue: $totalPending records', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                      FilledButton.icon(
                        onPressed: totalPending == 0 || !service.isOnline
                            ? null
                            : () {
                                service.triggerSync();
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(content: Text('All pending records pushed to server!'), backgroundColor: Color(0xFF059669)),
                                );
                              },
                        icon: const Icon(Icons.sync, size: 16),
                        label: const Text('Sync Now', style: TextStyle(fontSize: 12)),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Conflict Resolution Policy
          Card(
            elevation: 0,
            color: theme.colorScheme.surfaceVariant,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: const Padding(
              padding: EdgeInsets.all(12),
              child: Row(
                children: [
                  Icon(Icons.security, size: 20),
                  SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      'Conflict Policy: Client timestamp is authoritative for warehouse intake scanning; server manifest is authoritative for courier handover batches.',
                      style: TextStyle(fontSize: 11),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          Text('Pending Synchronization Queue', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),

          if (totalPending == 0)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 40),
              child: Center(
                child: Column(
                  children: [
                    Icon(Icons.cloud_done, size: 48, color: Colors.green),
                    SizedBox(height: 8),
                    Text('All records are currently up to date!'),
                  ],
                ),
              ),
            )
          else ...[
            ...pendingShipments.map((s) => Card(
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
                side: BorderSide(color: Colors.amber.shade400),
              ),
              child: ListTile(
                dense: true,
                leading: const Icon(Icons.inventory_2, color: Colors.amber),
                title: Text(s.trackingNumber, style: const TextStyle(fontWeight: FontWeight.bold, fontFamily: 'monospace')),
                subtitle: Text('New Parcel (${s.courier.displayName}) • Awaiting Cloud Push', style: const TextStyle(fontSize: 11)),
                trailing: const Chip(label: Text('Pending', style: TextStyle(fontSize: 9, color: Colors.amber)), visualDensity: VisualDensity.compact),
              ),
            )),
            ...pendingReturns.map((r) => Card(
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
                side: BorderSide(color: Colors.amber.shade400),
              ),
              child: ListTile(
                dense: true,
                leading: const Icon(Icons.assignment_return, color: Colors.amber),
                title: Text(r.trackingNumber, style: const TextStyle(fontWeight: FontWeight.bold, fontFamily: 'monospace')),
                subtitle: Text('RTS Return Record (${r.returnReason.label})', style: const TextStyle(fontSize: 11)),
                trailing: const Chip(label: Text('Pending', style: TextStyle(fontSize: 9, color: Colors.amber)), visualDensity: VisualDensity.compact),
              ),
            )),
          ],
        ],
      ),
    );
  }
}
