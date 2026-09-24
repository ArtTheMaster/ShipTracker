import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/logistics_service.dart';
import '../models/enums.dart';

class ReturnsScreen extends StatefulWidget {
  final LogisticsService service;

  const ReturnsScreen({super.key, required this.service});

  @override
  State<ReturnsScreen> createState() => _ReturnsScreenState();
}

class _ReturnsScreenState extends State<ReturnsScreen> {
  final _trackingController = TextEditingController();
  final _claimAmountController = TextEditingController(text: '0.00');
  final _notesController = TextEditingController();

  PlatformType _selectedPlatform = PlatformType.shopee;
  CourierType _selectedCourier = CourierType.ninjaVan;
  ReturnReason _selectedReason = ReturnReason.customerRejectedCod;
  ItemCondition _selectedCondition = ItemCondition.pristineResellable;

  @override
  void dispose() {
    _trackingController.dispose();
    _claimAmountController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  void _openLogReturnSheet(BuildContext context) {
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
                    const Text('Log Return-To-Sender (RTS) Parcel', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _trackingController,
                      decoration: const InputDecoration(
                        labelText: 'Returned Tracking Barcode *',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.qr_code),
                      ),
                    ),
                    const SizedBox(height: 10),
                    Row(
                      children: [
                        Expanded(
                          child: DropdownButtonFormField<PlatformType>(
                            value: _selectedPlatform,
                            decoration: const InputDecoration(labelText: 'Platform', border: OutlineInputBorder()),
                            items: PlatformType.values.map((p) => DropdownMenuItem(value: p, child: Text(p.displayName, style: const TextStyle(fontSize: 12)))).toList(),
                            onChanged: (val) {
                              if (val != null) setSheetState(() => _selectedPlatform = val);
                            },
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: DropdownButtonFormField<CourierType>(
                            value: _selectedCourier,
                            decoration: const InputDecoration(labelText: 'Courier', border: OutlineInputBorder()),
                            items: CourierType.values.map((c) => DropdownMenuItem(value: c, child: Text(c.displayName, style: const TextStyle(fontSize: 12), overflow: TextOverflow.ellipsis))).toList(),
                            onChanged: (val) {
                              if (val != null) setSheetState(() => _selectedCourier = val);
                            },
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    DropdownButtonFormField<ReturnReason>(
                      value: _selectedReason,
                      decoration: const InputDecoration(labelText: 'Return Reason *', border: OutlineInputBorder()),
                      items: ReturnReason.values.map((r) => DropdownMenuItem(value: r, child: Text(r.label, style: const TextStyle(fontSize: 12)))).toList(),
                      onChanged: (val) {
                        if (val != null) setSheetState(() => _selectedReason = val);
                      },
                    ),
                    const SizedBox(height: 10),
                    DropdownButtonFormField<ItemCondition>(
                      value: _selectedCondition,
                      decoration: const InputDecoration(labelText: 'Physical Condition *', border: OutlineInputBorder()),
                      items: ItemCondition.values.map((c) => DropdownMenuItem(value: c, child: Text(c.label, style: const TextStyle(fontSize: 12)))).toList(),
                      onChanged: (val) {
                        if (val != null) setSheetState(() => _selectedCondition = val);
                      },
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: _claimAmountController,
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      decoration: const InputDecoration(labelText: 'Claim / Refund Amount (₱)', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: _notesController,
                      decoration: const InputDecoration(labelText: 'Inspection Notes', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: FilledButton.icon(
                        icon: const Icon(Icons.assignment_return),
                        label: const Text('Log RTS Return Record'),
                        onPressed: _trackingController.text.trim().isEmpty
                            ? null
                            : () {
                                final claim = double.tryParse(_claimAmountController.text) ?? 0.0;
                                widget.service.logReturn(
                                  trackingNumber: _trackingController.text.trim().toUpperCase(),
                                  platform: _selectedPlatform,
                                  courier: _selectedCourier,
                                  reason: _selectedReason,
                                  condition: _selectedCondition,
                                  claimAmount: claim,
                                  notes: _notesController.text.trim(),
                                );
                                Navigator.pop(ctx);
                                _trackingController.clear();
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(content: Text('RTS record saved!'), backgroundColor: Color(0xFF059669)),
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

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final returns = widget.service.returns;
    final totalRts = returns.length;
    final approvedCount = returns.where((r) => r.refundStatus == RefundStatus.refundApproved).length;
    final disputeCount = returns.where((r) => r.refundStatus == RefundStatus.disputeRaised).length;
    final dateFormat = DateFormat('MMM dd, hh:mm a');

    return Scaffold(
      appBar: AppBar(
        title: const Text('Return-to-Sender (RTS) & Claims', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            tooltip: 'Log RTS',
            onPressed: () => _openLogReturnSheet(context),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openLogReturnSheet(context),
        icon: const Icon(Icons.assignment_return),
        label: const Text('Log RTS'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // KPI Row
          Row(
            children: [
              Expanded(child: _buildStatCard('Total RTS', '$totalRts', const Color(0xFFDC2626))),
              const SizedBox(width: 8),
              Expanded(child: _buildStatCard('Approved', '$approvedCount', const Color(0xFF059669))),
              const SizedBox(width: 8),
              Expanded(child: _buildStatCard('Disputed', '$disputeCount', const Color(0xFFD97706))),
            ],
          ),
          const SizedBox(height: 16),

          Text('Recent Returned Parcels', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),

          if (returns.isEmpty)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 40),
              child: Center(child: Text('No returns recorded yet.')),
            )
          else
            ...returns.map((item) => Card(
              elevation: 0,
              margin: const EdgeInsets.only(bottom: 10),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10),
                side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(item.trackingNumber, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, fontFamily: 'monospace')),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                          decoration: BoxDecoration(
                            color: item.refundStatus.color.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            item.refundStatus.label,
                            style: TextStyle(color: item.refundStatus.color, fontSize: 10, fontWeight: FontWeight.bold),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Text('${item.platform.displayName} • ${item.courier.displayName}', style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant)),
                    const SizedBox(height: 4),
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.errorContainer.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Reason: ${item.returnReason.label}', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11, color: theme.colorScheme.error)),
                          Text('Condition: ${item.itemCondition.label}', style: const TextStyle(fontSize: 11)),
                          if (item.notes.isNotEmpty) Text('Notes: ${item.notes}', style: TextStyle(fontSize: 10, color: theme.colorScheme.onSurfaceVariant)),
                        ],
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('Logged: ${dateFormat.format(item.loggedAt)} by ${item.loggedBy}', style: TextStyle(fontSize: 10, color: theme.colorScheme.onSurfaceVariant)),
                        if (item.claimAmount > 0)
                          Text('Claim: ₱${item.claimAmount.toStringAsFixed(2)}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                      ],
                    ),
                  ],
                ),
              ),
            )),
        ],
      ),
    );
  }

  Widget _buildStatCard(String label, String value, Color color) {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8), side: BorderSide(color: Colors.grey.shade300)),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
        child: Column(
          children: [
            Text(label, style: const TextStyle(fontSize: 11, color: Colors.grey)),
            const SizedBox(height: 4),
            Text(value, style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: color)),
          ],
        ),
      ),
    );
  }
}
