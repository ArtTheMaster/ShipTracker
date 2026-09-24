import 'package:flutter/material.dart';
import '../services/logistics_service.dart';
import '../models/enums.dart';

class ScannerScreen extends StatefulWidget {
  final LogisticsService service;

  const ScannerScreen({super.key, required this.service});

  @override
  State<ScannerScreen> createState() => _ScannerScreenState();
}

class _ScannerScreenState extends State<ScannerScreen> {
  final _formKey = GlobalKey<FormState>();
  final _trackingController = TextEditingController();
  final _recipientNameController = TextEditingController();
  final _recipientPhoneController = TextEditingController();
  final _recipientAddressController = TextEditingController();
  final _itemsController = TextEditingController();
  final _weightController = TextEditingController(text: '0.50');
  final _declaredValueController = TextEditingController(text: '500.00');

  PlatformType _selectedPlatform = PlatformType.shopee;
  CourierType _selectedCourier = CourierType.spx;
  String _ruleMatched = 'Awaiting input...';

  @override
  void initState() {
    super.initState();
    _trackingController.addListener(_onTrackingChanged);
  }

  @override
  void dispose() {
    _trackingController.removeListener(_onTrackingChanged);
    _trackingController.dispose();
    _recipientNameController.dispose();
    _recipientPhoneController.dispose();
    _recipientAddressController.dispose();
    _itemsController.dispose();
    _weightController.dispose();
    _declaredValueController.dispose();
    super.dispose();
  }

  void _onTrackingChanged() {
    final text = _trackingController.text;
    if (text.isNotEmpty) {
      final detected = widget.service.detectCourierAndPlatform(text);
      setState(() {
        _selectedCourier = detected['courier'] as CourierType;
        _selectedPlatform = detected['platform'] as PlatformType;
        _ruleMatched = detected['rule'] as String;
      });
    }
  }

  void _fillSampleBarcode(String sample) {
    _trackingController.text = sample;
    _recipientNameController.text = 'Maria Clara Santos';
    _recipientPhoneController.text = '+63 917 888 1234';
    _recipientAddressController.text = 'Unit 201, Greenhills Courtyard, San Juan, Metro Manila';
    _itemsController.text = 'Ergonomic Desk Mat + Wrist Rest';
    _weightController.text = '0.75';
    _declaredValueController.text = '1250.00';
  }

  void _saveParcel() {
    if (!_formKey.currentState!.validate()) return;

    final weight = double.tryParse(_weightController.text) ?? 0.5;
    final value = double.tryParse(_declaredValueController.text) ?? 0.0;

    widget.service.addShipment(
      trackingNumber: _trackingController.text,
      recipientName: _recipientNameController.text,
      recipientPhone: _recipientPhoneController.text,
      recipientAddress: _recipientAddressController.text,
      platform: _selectedPlatform,
      courier: _selectedCourier,
      weightKg: weight,
      declaredValue: value,
      itemsSummary: _itemsController.text,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Parcel ${_trackingController.text.toUpperCase()} registered successfully!'),
        backgroundColor: const Color(0xFF059669),
      ),
    );

    _trackingController.clear();
    _recipientNameController.clear();
    _recipientPhoneController.clear();
    _recipientAddressController.clear();
    _itemsController.clear();
    _weightController.text = '0.50';
    _declaredValueController.text = '500.00';
    setState(() {
      _ruleMatched = 'Awaiting input...';
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Parcel Intake Scanner', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Barcode Viewfinder simulation
            Container(
              height: 140,
              decoration: BoxDecoration(
                color: Colors.black,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: theme.colorScheme.primary, width: 2),
              ),
              child: Stack(
                children: [
                  Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.qr_code_scanner, color: Colors.white, size: 42),
                        const SizedBox(height: 8),
                        Text(
                          'Point camera at parcel waybill barcode',
                          style: TextStyle(color: Colors.white.withOpacity(0.8), fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                  Positioned(
                    bottom: 8,
                    left: 0,
                    right: 0,
                    child: Center(
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: Colors.black.withOpacity(0.7),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          'Auto-detection: $_ruleMatched',
                          style: const TextStyle(color: Color(0xFF38BDF8), fontSize: 10, fontWeight: FontWeight.bold),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),

            // Quick Test Sample Barcode Chips
            Text('Quick Fill Test Barcodes:', style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant)),
            const SizedBox(height: 6),
            Wrap(
              spacing: 6,
              runSpacing: 4,
              children: [
                ActionChip(
                  label: const Text('Shopee SPX', style: TextStyle(fontSize: 11)),
                  onPressed: () => _fillSampleBarcode('SPXPH0992381289A'),
                ),
                ActionChip(
                  label: const Text('Lazada LEX', style: TextStyle(fontSize: 11)),
                  onPressed: () => _fillSampleBarcode('MP9921820491LEX'),
                ),
                ActionChip(
                  label: const Text('J&T TikTok', style: TextStyle(fontSize: 11)),
                  onPressed: () => _fillSampleBarcode('JNET8810293812PH'),
                ),
                ActionChip(
                  label: const Text('Flash Express', style: TextStyle(fontSize: 11)),
                  onPressed: () => _fillSampleBarcode('FL7710293819PH'),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Tracking Barcode Input
            TextFormField(
              controller: _trackingController,
              textCapitalization: TextCapitalization.characters,
              decoration: const InputDecoration(
                labelText: 'Waybill / Tracking Barcode Number *',
                prefixIcon: Icon(Icons.barcode_reader),
                border: OutlineInputBorder(),
              ),
              validator: (val) => val == null || val.trim().isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 12),

            // Platform and Courier Selectors
            Row(
              children: [
                Expanded(
                  child: DropdownButtonFormField<PlatformType>(
                    value: _selectedPlatform,
                    decoration: const InputDecoration(labelText: 'Platform', border: OutlineInputBorder()),
                    items: PlatformType.values.map((p) {
                      return DropdownMenuItem(value: p, child: Text(p.displayName, style: const TextStyle(fontSize: 13)));
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) setState(() => _selectedPlatform = val);
                    },
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: DropdownButtonFormField<CourierType>(
                    value: _selectedCourier,
                    decoration: const InputDecoration(labelText: 'Courier', border: OutlineInputBorder()),
                    items: CourierType.values.map((c) {
                      return DropdownMenuItem(value: c, child: Text(c.displayName, style: const TextStyle(fontSize: 13), overflow: TextOverflow.ellipsis));
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) setState(() => _selectedCourier = val);
                    },
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),

            // Customer Name & Phone
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    controller: _recipientNameController,
                    decoration: const InputDecoration(labelText: 'Recipient Full Name *', border: OutlineInputBorder()),
                    validator: (val) => val == null || val.trim().isEmpty ? 'Required' : null,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: TextFormField(
                    controller: _recipientPhoneController,
                    decoration: const InputDecoration(labelText: 'Phone Number *', border: OutlineInputBorder()),
                    validator: (val) => val == null || val.trim().isEmpty ? 'Required' : null,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),

            // Address
            TextFormField(
              controller: _recipientAddressController,
              decoration: const InputDecoration(labelText: 'Delivery Address *', border: OutlineInputBorder()),
              maxLines: 2,
              validator: (val) => val == null || val.trim().isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 12),

            // Weight & Declared Value
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    controller: _weightController,
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(labelText: 'Weight (kg) *', border: OutlineInputBorder()),
                    validator: (val) => val == null || double.tryParse(val) == null ? 'Valid number required' : null,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: TextFormField(
                    controller: _declaredValueController,
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(labelText: 'Declared Value (₱) *', border: OutlineInputBorder()),
                    validator: (val) => val == null || double.tryParse(val) == null ? 'Valid number required' : null,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),

            // Items Summary
            TextFormField(
              controller: _itemsController,
              decoration: const InputDecoration(labelText: 'Parcel Contents Summary *', border: OutlineInputBorder()),
              validator: (val) => val == null || val.trim().isEmpty ? 'Required' : null,
            ),
            const SizedBox(height: 20),

            FilledButton.icon(
              onPressed: _saveParcel,
              icon: const Icon(Icons.save),
              label: const Text('Register & Save Outbound Parcel'),
              style: FilledButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 14)),
            ),
          ],
        ),
      ),
    );
  }
}
