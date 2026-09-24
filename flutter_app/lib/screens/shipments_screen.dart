import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/logistics_service.dart';
import '../models/shipment.dart';
import '../models/enums.dart';

class ShipmentsScreen extends StatefulWidget {
  final LogisticsService service;

  const ShipmentsScreen({super.key, required this.service});

  @override
  State<ShipmentsScreen> createState() => _ShipmentsScreenState();
}

class _ShipmentsScreenState extends State<ShipmentsScreen> {
  String _searchQuery = '';
  ShipmentStatus? _selectedStatusFilter;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final allShipments = widget.service.shipments;
    final currencyFormat = NumberFormat.currency(symbol: '₱', decimalDigits: 2);
    final dateFormat = DateFormat('MMM dd, hh:mm a');

    final filtered = allShipments.where((s) {
      final matchesSearch = s.trackingNumber.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          s.recipientName.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          s.itemsSummary.toLowerCase().contains(_searchQuery.toLowerCase());
      final matchesStatus = _selectedStatusFilter == null || s.status == _selectedStatusFilter;
      return matchesSearch && matchesStatus;
    }).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Outbound Parcel Registry', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
      ),
      body: Column(
        children: [
          // Search & Filter Bar
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
            child: TextField(
              decoration: InputDecoration(
                hintText: 'Search barcode or recipient...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchQuery.isNotEmpty
                    ? IconButton(icon: const Icon(Icons.clear), onPressed: () => setState(() => _searchQuery = ''))
                    : null,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                contentPadding: const EdgeInsets.symmetric(vertical: 0, horizontal: 12),
              ),
              onChanged: (val) => setState(() => _searchQuery = val),
            ),
          ),

          // Horizontal Filter Chips
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Row(
              children: [
                FilterChip(
                  label: const Text('All'),
                  selected: _selectedStatusFilter == null,
                  onSelected: (val) => setState(() => _selectedStatusFilter = null),
                ),
                const SizedBox(width: 6),
                ...ShipmentStatus.values.map((status) => Padding(
                  padding: const EdgeInsets.only(right: 6.0),
                  child: FilterChip(
                    label: Text(status.displayName),
                    selected: _selectedStatusFilter == status,
                    onSelected: (val) => setState(() => _selectedStatusFilter = val ? status : null),
                  ),
                )),
              ],
            ),
          ),
          const SizedBox(height: 6),

          // Count Summary Bar
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 4.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('${filtered.length} Parcels Found', style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant)),
                if (widget.service.currentUser.role == UserRole.staffPacker)
                  Text('Staff Role: Deletion Disabled', style: TextStyle(fontSize: 10, color: theme.colorScheme.primary)),
              ],
            ),
          ),

          // Shipments List
          Expanded(
            child: filtered.isEmpty
                ? Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.inventory_2_outlined, size: 48, color: theme.colorScheme.outline),
                        const SizedBox(height: 8),
                        Text('No shipments matching query.', style: TextStyle(color: theme.colorScheme.onSurfaceVariant)),
                      ],
                    ),
                  )
                : ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: filtered.length,
                    itemBuilder: (context, index) {
                      final item = filtered[index];
                      return Card(
                        elevation: 0,
                        margin: const EdgeInsets.only(bottom: 10),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                          side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
                        ),
                        child: InkWell(
                          borderRadius: BorderRadius.circular(10),
                          onTap: () => _showShipmentDetails(context, item),
                          child: Padding(
                            padding: const EdgeInsets.all(12),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                                      decoration: BoxDecoration(
                                        color: item.courier.brandColor.withOpacity(0.12),
                                        borderRadius: BorderRadius.circular(6),
                                      ),
                                      child: Text(
                                        item.courier.displayName,
                                        style: TextStyle(
                                          color: item.courier.brandColor,
                                          fontWeight: FontWeight.bold,
                                          fontSize: 11,
                                        ),
                                      ),
                                    ),
                                    _buildStatusBadge(item.status),
                                  ],
                                ),
                                const SizedBox(height: 8),
                                Text(
                                  item.trackingNumber,
                                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, fontFamily: 'monospace'),
                                ),
                                const SizedBox(height: 4),
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text('Recipient: ${item.recipientName}', style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant)),
                                    Text('${item.weightKg} kg • ${currencyFormat.format(item.declaredValue)}', style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                                  ],
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  item.recipientAddress,
                                  style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                                const Divider(height: 16),
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text('Scanned: ${dateFormat.format(item.scannedAt)}', style: TextStyle(fontSize: 10, color: theme.colorScheme.onSurfaceVariant)),
                                    Row(
                                      children: [
                                        TextButton(
                                          onPressed: () => _showStatusTransitionDialog(context, item),
                                          style: TextButton.styleFrom(visualDensity: VisualDensity.compact),
                                          child: const Text('Update Status', style: TextStyle(fontSize: 11)),
                                        ),
                                        if (widget.service.currentUser.role.canDeleteShipments)
                                          IconButton(
                                            icon: const Icon(Icons.delete_outline, size: 18),
                                            color: Colors.red,
                                            onPressed: () => _confirmDelete(context, item.trackingNumber),
                                          ),
                                      ],
                                    ),
                                  ],
                                ),
                              ],
                            ),
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusBadge(ShipmentStatus status) {
    Color bg = const Color(0xFFE2E8F0);
    Color fg = const Color(0xFF334155);

    switch (status) {
      case ShipmentStatus.scanned:
        bg = const Color(0xFFE0E7FF);
        fg = const Color(0xFF3730A3);
        break;
      case ShipmentStatus.prepared:
        bg = const Color(0xFFCCFBF1);
        fg = const Color(0xFF115E59);
        break;
      case ShipmentStatus.dispatched:
        bg = const Color(0xFFDBEAFE);
        fg = const Color(0xFF1E40AF);
        break;
      case ShipmentStatus.inTransit:
        bg = const Color(0xFFFEF3C7);
        fg = const Color(0xFF92400E);
        break;
      case ShipmentStatus.delivered:
        bg = const Color(0xFFD1FAE5);
        fg = const Color(0xFF065F46);
        break;
      case ShipmentStatus.returned:
        bg = const Color(0xFFFEE2E2);
        fg = const Color(0xFF991B1B);
        break;
      case ShipmentStatus.cancelled:
        bg = const Color(0xFFF3F4F6);
        fg = const Color(0xFF4B5563);
        break;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(12)),
      child: Text(status.displayName, style: TextStyle(color: fg, fontSize: 10, fontWeight: FontWeight.bold)),
    );
  }

  void _showStatusTransitionDialog(BuildContext context, Shipment shipment) {
    showDialog(
      context: context,
      builder: (ctx) {
        return AlertDialog(
          title: Text('Update Status: ${shipment.trackingNumber}', style: const TextStyle(fontSize: 14)),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: ShipmentStatus.values.map((st) {
              return ListTile(
                dense: true,
                title: Text(st.displayName),
                trailing: shipment.status == st ? const Icon(Icons.check, color: Colors.green) : null,
                onTap: () {
                  widget.service.updateShipmentStatus(shipment.trackingNumber, st);
                  Navigator.pop(ctx);
                },
              );
            }).toList(),
          ),
        );
      },
    );
  }

  void _confirmDelete(BuildContext context, String trackingNumber) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Confirm Deletion'),
        content: Text('Are you sure you want to delete parcel $trackingNumber?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () {
              widget.service.deleteShipment(trackingNumber);
              Navigator.pop(ctx);
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  void _showShipmentDetails(BuildContext context, Shipment shipment) {
    final dateFormat = DateFormat('yyyy-MM-dd HH:mm:ss');
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (ctx) {
        return Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Parcel Details & Audit Audit Trail', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),
              Text('Tracking: ${shipment.trackingNumber}', style: const TextStyle(fontWeight: FontWeight.bold, fontFamily: 'monospace')),
              Text('Courier: ${shipment.courier.displayName} (${shipment.platform.displayName})'),
              Text('Recipient: ${shipment.recipientName} (${shipment.recipientPhone})'),
              Text('Address: ${shipment.recipientAddress}'),
              Text('Items: ${shipment.itemsSummary}'),
              Text('Weight: ${shipment.weightKg} kg  •  Declared: ₱${shipment.declaredValue}'),
              const Divider(height: 20),
              const Text('Lifecycle Timestamps:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
              Text('Scanned at: ${dateFormat.format(shipment.scannedAt)} by ${shipment.scannedBy}'),
              if (shipment.packedAt != null) Text('Packed at: ${dateFormat.format(shipment.packedAt!)}'),
              if (shipment.dispatchedAt != null) Text('Dispatched at: ${dateFormat.format(shipment.dispatchedAt!)}'),
              if (shipment.completedAt != null) Text('Completed/Delivered at: ${dateFormat.format(shipment.completedAt!)}'),
              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                child: FilledButton(onPressed: () => Navigator.pop(ctx), child: const Text('Close')),
              ),
            ],
          ),
        );
      },
    );
  }
}
