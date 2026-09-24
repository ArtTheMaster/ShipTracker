import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/logistics_service.dart';
import '../models/enums.dart';

class DispatchScreen extends StatefulWidget {
  final LogisticsService service;

  const DispatchScreen({super.key, required this.service});

  @override
  State<DispatchScreen> createState() => _DispatchScreenState();
}

class _DispatchScreenState extends State<DispatchScreen> {
  final _driverNameController = TextEditingController();
  final _driverPlateController = TextEditingController();
  final _driverPhoneController = TextEditingController();
  final _notesController = TextEditingController();

  CourierType _selectedCourier = CourierType.jAndT;
  final Set<String> _selectedParcels = {};

  @override
  void dispose() {
    _driverNameController.dispose();
    _driverPlateController.dispose();
    _driverPhoneController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  void _openCreateBatchSheet(BuildContext context) {
    // Candidates are parcels that are scanned or prepared (not yet dispatched)
    final candidates = widget.service.shipments
        .where((s) => s.status == ShipmentStatus.scanned || s.status == ShipmentStatus.prepared)
        .toList();

    _selectedParcels.clear();

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (ctx) {
        return StatefulBuilder(
          builder: (context, setSheetState) {
            return Padding(
              padding: EdgeInsets.only(
                top: 20,
                left: 16,
                right: 16,
                bottom: MediaQuery.of(ctx).viewInsets.bottom + 20,
              ),
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Generate Courier Handover Batch', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<CourierType>(
                      value: _selectedCourier,
                      decoration: const InputDecoration(labelText: 'Assigned Courier', border: OutlineInputBorder()),
                      items: CourierType.values.map((c) => DropdownMenuItem(value: c, child: Text(c.displayName))).toList(),
                      onChanged: (val) {
                        if (val != null) setSheetState(() => _selectedCourier = val);
                      },
                    ),
                    const SizedBox(height: 10),
                    Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: _driverNameController,
                            decoration: const InputDecoration(labelText: 'Rider Name *', border: OutlineInputBorder()),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: TextField(
                            controller: _driverPlateController,
                            decoration: const InputDecoration(labelText: 'Plate / Vehicle *', border: OutlineInputBorder()),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: _driverPhoneController,
                      decoration: const InputDecoration(labelText: 'Rider Contact Number', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 12),
                    Text('Select Outbound Parcels (${_selectedParcels.length} selected):', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                    const SizedBox(height: 6),
                    if (candidates.isEmpty)
                      const Padding(
                        padding: EdgeInsets.symmetric(vertical: 12),
                        child: Text('No scanned/packed parcels available for handover.', style: TextStyle(color: Colors.grey)),
                      )
                    else
                      Container(
                        constraints: const BoxConstraints(maxHeight: 180),
                        decoration: BoxDecoration(
                          border: Border.all(color: Colors.grey.shade300),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: ListView.builder(
                          shrinkWrap: true,
                          itemCount: candidates.length,
                          itemBuilder: (c, idx) {
                            final p = candidates[idx];
                            final isChecked = _selectedParcels.contains(p.trackingNumber);
                            return CheckboxListTile(
                              dense: true,
                              value: isChecked,
                              title: Text(p.trackingNumber, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                              subtitle: Text('${p.courier.displayName} • ${p.weightKg} kg • ${p.recipientName}', style: const TextStyle(fontSize: 10)),
                              onChanged: (val) {
                                setSheetState(() {
                                  if (val == true) {
                                    _selectedParcels.add(p.trackingNumber);
                                  } else {
                                    _selectedParcels.remove(p.trackingNumber);
                                  }
                                });
                              },
                            );
                          },
                        ),
                      ),
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: FilledButton.icon(
                        icon: const Icon(Icons.send),
                        label: const Text('Confirm Handover & Seal Manifest'),
                        onPressed: _selectedParcels.isEmpty || _driverNameController.text.isEmpty
                            ? null
                            : () {
                                widget.service.createDispatchBatch(
                                  courier: _selectedCourier,
                                  driverName: _driverNameController.text,
                                  driverPlateNumber: _driverPlateController.text,
                                  driverPhone: _driverPhoneController.text,
                                  trackingNumbers: _selectedParcels.toList(),
                                  notes: _notesController.text,
                                );
                                Navigator.pop(ctx);
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(content: Text('Dispatch batch created successfully!'), backgroundColor: Color(0xFF059669)),
                                );
                              },
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  void _showManifest(BuildContext context, dynamic batch) {
    final dateFormat = DateFormat('yyyy-MM-dd HH:mm');
    final manifestText = StringBuffer();
    manifestText.writeln('========================================');
    manifestText.writeln('  GJandAsher Outbound Dispatch Manifest  ');
    manifestText.writeln('========================================');
    manifestText.writeln('Batch Code:   ${batch.batchNumber}');
    manifestText.writeln('Courier:      ${batch.courier.displayName}');
    manifestText.writeln('Rider Name:   ${batch.driverName}');
    manifestText.writeln('Plate Number: ${batch.driverPlateNumber}');
    manifestText.writeln('Phone:        ${batch.driverPhone}');
    manifestText.writeln('Handover:     ${dateFormat.format(batch.handoverTimestamp)}');
    manifestText.writeln('Parcel Count: ${batch.parcelCount}');
    manifestText.writeln('Total Weight: ${batch.totalWeightKg.toStringAsFixed(2)} kg');
    manifestText.writeln('Dispatched by: ${batch.createdBy}');
    manifestText.writeln('----------------------------------------');
    manifestText.writeln('Rider Signature: _______________________');
    manifestText.writeln('Warehouse Staff: _______________________');
    manifestText.writeln('========================================');

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('Manifest: ${batch.batchNumber}', style: const TextStyle(fontSize: 14)),
        content: SingleChildScrollView(
          child: Text(
            manifestText.toString(),
            style: const TextStyle(fontFamily: 'monospace', fontSize: 11),
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Close')),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final batches = widget.service.batches;
    final timeFormat = DateFormat('MMM dd, hh:mm a');

    return Scaffold(
      appBar: AppBar(
        title: const Text('Courier Handover & Dispatch', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            tooltip: 'New Handover Batch',
            onPressed: () => _openCreateBatchSheet(context),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openCreateBatchSheet(context),
        icon: const Icon(Icons.handshake),
        label: const Text('New Batch'),
      ),
      body: batches.isEmpty
          ? Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.local_shipping_outlined, size: 48, color: theme.colorScheme.outline),
                  const SizedBox(height: 8),
                  const Text('No handover batches generated yet.'),
                ],
              ),
            )
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: batches.length,
              itemBuilder: (ctx, idx) {
                final b = batches[idx];
                return Card(
                  elevation: 0,
                  margin: const EdgeInsets.only(bottom: 12),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                    side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(14),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(b.batchNumber, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, fontFamily: 'monospace')),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                              decoration: BoxDecoration(color: const Color(0xFFD1FAE5), borderRadius: BorderRadius.circular(10)),
                              child: const Text('HANDED OVER', style: TextStyle(color: Color(0xFF065F46), fontSize: 9, fontWeight: FontWeight.bold)),
                            ),
                          ],
                        ),
                        const SizedBox(height: 6),
                        Text('Courier: ${b.courier.displayName}'),
                        Text('Rider: ${b.driverName} (${b.driverPlateNumber})', style: const TextStyle(fontWeight: FontWeight.bold)),
                        Text('Contact: ${b.driverPhone}', style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant)),
                        Text('Dispatched: ${timeFormat.format(b.handoverTimestamp)} by ${b.createdBy}', style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant)),
                        const Divider(height: 16),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text('${b.parcelCount} Parcels  •  ${b.totalWeightKg.toStringAsFixed(2)} kg', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                            OutlinedButton.icon(
                              onPressed: () => _showManifest(context, b),
                              icon: const Icon(Icons.print, size: 14),
                              label: const Text('View Manifest', style: TextStyle(fontSize: 11)),
                              style: OutlinedButton.styleFrom(visualDensity: VisualDensity.compact),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
    );
  }
}
