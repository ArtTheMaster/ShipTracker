import 'dart:math';
import 'package:flutter/foundation.dart';
import '../models/enums.dart';
import '../models/user_account.dart';
import '../models/shipment.dart';
import '../models/dispatch_batch.dart';
import '../models/return_record.dart';
import '../models/audit_log.dart';

class LogisticsService extends ChangeNotifier {
  UserAccount _currentUser = UserAccount.defaultAdmin;
  UserAccount get currentUser => _currentUser;

  bool _isOnline = true;
  bool get isOnline => _isOnline;

  List<Shipment> _shipments = [];
  List<Shipment> get shipments => List.unmodifiable(_shipments);

  List<DispatchBatch> _batches = [];
  List<DispatchBatch> get batches => List.unmodifiable(_batches);

  List<ReturnRecord> _returns = [];
  List<ReturnRecord> get returns => List.unmodifiable(_returns);

  List<AuditLog> _logs = [];
  List<AuditLog> get logs => List.unmodifiable(_logs);

  int _nextLogId = 1;

  LogisticsService() {
    _seedInitialData();
  }

  void switchAccount(UserAccount account) {
    _currentUser = account;
    _addLog('ROLE_SWITCH', 'Switched session to ${account.fullName} (${account.role.displayName})');
    notifyListeners();
  }

  void toggleNetworkConnection() {
    _isOnline = !_isOnline;
    _addLog('NETWORK_CHANGE', _isOnline ? 'Network restored (Online mode)' : 'Simulating offline warehouse environment');
    notifyListeners();
  }

  void triggerSync() {
    bool hasUpdates = false;
    _shipments = _shipments.map((s) {
      if (s.syncStatus == SyncStatus.pendingSync) {
        hasUpdates = true;
        return s.copyWith(syncStatus: SyncStatus.synced);
      }
      return s;
    }).toList();

    _returns = _returns.map((r) {
      if (r.syncStatus == SyncStatus.pendingSync) {
        hasUpdates = true;
        return ReturnRecord(
          returnId: r.returnId,
          trackingNumber: r.trackingNumber,
          platform: r.platform,
          courier: r.courier,
          returnReason: r.returnReason,
          itemCondition: r.itemCondition,
          refundStatus: r.refundStatus,
          claimAmount: r.claimAmount,
          notes: r.notes,
          loggedBy: r.loggedBy,
          loggedAt: r.loggedAt,
          syncStatus: SyncStatus.synced,
        );
      }
      return r;
    }).toList();

    if (hasUpdates) {
      _addLog('CLOUD_SYNC', 'Synchronized pending warehouse records with central server');
    }
    notifyListeners();
  }

  // --- Parcel Operations ---
  void addShipment({
    required String trackingNumber,
    required String recipientName,
    required String recipientPhone,
    required String recipientAddress,
    required PlatformType platform,
    required CourierType courier,
    required double weightKg,
    required double declaredValue,
    required String itemsSummary,
  }) {
    final now = DateTime.now();
    final newShipment = Shipment(
      trackingNumber: trackingNumber.trim().toUpperCase(),
      recipientName: recipientName.trim(),
      recipientPhone: recipientPhone.trim(),
      recipientAddress: recipientAddress.trim(),
      platform: platform,
      courier: courier,
      weightKg: weightKg,
      declaredValue: declaredValue,
      itemsSummary: itemsSummary.trim(),
      status: ShipmentStatus.scanned,
      scannedAt: now,
      scannedBy: _currentUser.fullName,
      syncStatus: _isOnline ? SyncStatus.synced : SyncStatus.pendingSync,
      lastModifiedAt: now,
    );

    _shipments.insert(0, newShipment);
    _addLog('PARCEL_SCANNED', 'Registered outbound barcode $trackingNumber for ${courier.displayName}');
    notifyListeners();
  }

  void updateShipmentStatus(String trackingNumber, ShipmentStatus newStatus) {
    final index = _shipments.indexWhere((s) => s.trackingNumber == trackingNumber);
    if (index == -1) return;

    final current = _shipments[index];
    final now = DateTime.now();

    DateTime? packedAt = current.packedAt;
    DateTime? dispatchedAt = current.dispatchedAt;
    DateTime? inTransitAt = current.inTransitAt;
    DateTime? completedAt = current.completedAt;

    if (newStatus == ShipmentStatus.prepared && packedAt == null) packedAt = now;
    if (newStatus == ShipmentStatus.dispatched && dispatchedAt == null) dispatchedAt = now;
    if (newStatus == ShipmentStatus.inTransit && inTransitAt == null) inTransitAt = now;
    if ((newStatus == ShipmentStatus.delivered || newStatus == ShipmentStatus.returned) && completedAt == null) {
      completedAt = now;
    }

    _shipments[index] = current.copyWith(
      status: newStatus,
      packedAt: packedAt,
      dispatchedAt: dispatchedAt,
      inTransitAt: inTransitAt,
      completedAt: completedAt,
      syncStatus: _isOnline ? SyncStatus.synced : SyncStatus.pendingSync,
      lastModifiedAt: now,
    );

    _addLog('STATUS_UPDATE', 'Parcel $trackingNumber transitioned to ${newStatus.displayName}');
    notifyListeners();
  }

  bool deleteShipment(String trackingNumber) {
    if (!_currentUser.role.canDeleteShipments) return false;
    _shipments.removeWhere((s) => s.trackingNumber == trackingNumber);
    _addLog('PARCEL_DELETED', 'Admin deleted parcel record $trackingNumber');
    notifyListeners();
    return true;
  }

  // --- Dispatch Batching ---
  DispatchBatch createDispatchBatch({
    required CourierType courier,
    required String driverName,
    required String driverPlateNumber,
    required String driverPhone,
    required List<String> trackingNumbers,
    String notes = '',
  }) {
    final now = DateTime.now();
    final batchCode = 'BCH-${courier.name.toUpperCase().substring(0, min(3, courier.name.length))}-${now.millisecondsSinceEpoch.toString().substring(7)}';
    
    double totalWeight = 0.0;
    for (var tn in trackingNumbers) {
      final s = _shipments.firstWhere((item) => item.trackingNumber == tn, orElse: () => _shipments.first);
      totalWeight += s.weightKg;
    }

    final newBatch = DispatchBatch(
      batchId: 'BATCH-${DateTime.now().millisecondsSinceEpoch}',
      batchNumber: batchCode,
      courier: courier,
      driverName: driverName,
      driverPlateNumber: driverPlateNumber,
      driverPhone: driverPhone,
      handoverTimestamp: now,
      parcelCount: trackingNumbers.length,
      totalWeightKg: totalWeight,
      notes: notes,
      createdBy: _currentUser.fullName,
      syncStatus: _isOnline ? SyncStatus.synced : SyncStatus.pendingSync,
    );

    _batches.insert(0, newBatch);

    // Update member shipments
    for (var tn in trackingNumbers) {
      final idx = _shipments.indexWhere((s) => s.trackingNumber == tn);
      if (idx != -1) {
        _shipments[idx] = _shipments[idx].copyWith(
          batchId: newBatch.batchId,
          status: ShipmentStatus.dispatched,
          dispatchedAt: now,
          syncStatus: _isOnline ? SyncStatus.synced : SyncStatus.pendingSync,
          lastModifiedAt: now,
        );
      }
    }

    _addLog('DISPATCH_HANDOVER', 'Dispatched batch $batchCode with ${trackingNumbers.length} parcels to rider $driverName');
    notifyListeners();
    return newBatch;
  }

  // --- Return Logging ---
  void logReturn({
    required String trackingNumber,
    required PlatformType platform,
    required CourierType courier,
    required ReturnReason reason,
    required ItemCondition condition,
    required double claimAmount,
    String notes = '',
  }) {
    final now = DateTime.now();
    final returnRecord = ReturnRecord(
      returnId: 'RTS-${now.millisecondsSinceEpoch}',
      trackingNumber: trackingNumber,
      platform: platform,
      courier: courier,
      returnReason: reason,
      itemCondition: condition,
      refundStatus: RefundStatus.pendingVerification,
      claimAmount: claimAmount,
      notes: notes,
      loggedBy: _currentUser.fullName,
      loggedAt: now,
      syncStatus: _isOnline ? SyncStatus.synced : SyncStatus.pendingSync,
    );

    _returns.insert(0, returnRecord);

    final idx = _shipments.indexWhere((s) => s.trackingNumber == trackingNumber);
    if (idx != -1) {
      _shipments[idx] = _shipments[idx].copyWith(
        status: ShipmentStatus.returned,
        completedAt: now,
        syncStatus: _isOnline ? SyncStatus.synced : SyncStatus.pendingSync,
        lastModifiedAt: now,
      );
    }

    _addLog('RETURN_LOGGED', 'RTS record filed for $trackingNumber (${reason.label})');
    notifyListeners();
  }

  // --- Pattern Matcher for Tracking Barcodes ---
  Map<String, dynamic> detectCourierAndPlatform(String code) {
    final clean = code.trim().toUpperCase();
    if (clean.startsWith('SPXPH') || clean.startsWith('SPX')) {
      return {'courier': CourierType.spx, 'platform': PlatformType.shopee, 'rule': 'SPX Prefix Match'};
    } else if (clean.startsWith('MP') || clean.startsWith('LEX')) {
      return {'courier': CourierType.lazadaLex, 'platform': PlatformType.lazada, 'rule': 'LEX/MP Logistics Match'};
    } else if (clean.startsWith('JNET') || clean.startsWith('JT') || clean.startsWith('77')) {
      return {'courier': CourierType.jAndT, 'platform': PlatformType.tiktokShop, 'rule': 'J&T Prefix Match'};
    } else if (clean.startsWith('TH') || clean.startsWith('FL')) {
      return {'courier': CourierType.flashExpress, 'platform': PlatformType.tiktokShop, 'rule': 'Flash Express Prefix'};
    } else if (clean.startsWith('NINJA') || clean.startsWith('NVPH')) {
      return {'courier': CourierType.ninjaVan, 'platform': PlatformType.shopee, 'rule': 'Ninja Van Prefix'};
    }
    return {'courier': CourierType.other, 'platform': PlatformType.other, 'rule': 'Generic Barcode'};
  }

  void _addLog(String action, String details) {
    _logs.insert(
      0,
      AuditLog(
        id: _nextLogId++,
        action: action,
        details: details,
        performedBy: _currentUser.fullName,
        timestamp: DateTime.now(),
      ),
    );
  }

  void _seedInitialData() {
    final now = DateTime.now();

    _shipments = [
      Shipment(
        trackingNumber: 'SPXPH04928172901B',
        recipientName: 'Juan Dela Cruz',
        recipientPhone: '+63 917 123 4567',
        recipientAddress: 'Unit 402, Acacia Bldg, BGC, Taguig City',
        platform: PlatformType.shopee,
        courier: CourierType.spx,
        weightKg: 0.85,
        declaredValue: 1450.0,
        itemsSummary: 'Wireless Mechanical Keyboard (Blue Switch)',
        status: ShipmentStatus.scanned,
        scannedAt: now.subtract(const Duration(minutes: 15)),
        scannedBy: 'Nolan (Owner)',
        lastModifiedAt: now,
      ),
      Shipment(
        trackingNumber: 'MP0491823901LEX',
        recipientName: 'Maria Clara Lopez',
        recipientPhone: '+63 928 555 9812',
        recipientAddress: '12 Emerald St, San Antonio Village, Pasig City',
        platform: PlatformType.lazada,
        courier: CourierType.lazadaLex,
        weightKg: 1.20,
        declaredValue: 2890.0,
        itemsSummary: 'Ergonomic Mesh Office Chair Cushion',
        status: ShipmentStatus.prepared,
        scannedAt: now.subtract(const Duration(hours: 1)),
        packedAt: now.subtract(const Duration(minutes: 30)),
        scannedBy: 'G.J. (Packer-Scanner)',
        lastModifiedAt: now,
      ),
      Shipment(
        trackingNumber: 'JNET9910481239PH',
        recipientName: 'Carlos Mendoza',
        recipientPhone: '+63 908 444 1122',
        recipientAddress: 'Lot 5 Block 3, Vista Verde, Cainta, Rizal',
        platform: PlatformType.tiktokShop,
        courier: CourierType.jAndT,
        weightKg: 0.45,
        declaredValue: 850.0,
        itemsSummary: 'RGB Gaming Headset with Microphone',
        status: ShipmentStatus.dispatched,
        batchId: 'BATCH-INITIAL-01',
        scannedAt: now.subtract(const Duration(hours: 3)),
        packedAt: now.subtract(const Duration(hours: 2)),
        dispatchedAt: now.subtract(const Duration(hours: 1)),
        scannedBy: 'Nolan (Owner)',
        lastModifiedAt: now,
      ),
      Shipment(
        trackingNumber: 'FL8829103948PH',
        recipientName: 'Beatriz Gomez',
        recipientPhone: '+63 919 777 3344',
        recipientAddress: '45 Quezon Ave, Diliman, Quezon City',
        platform: PlatformType.tiktokShop,
        courier: CourierType.flashExpress,
        weightKg: 2.10,
        declaredValue: 4200.0,
        itemsSummary: 'Dual Monitor Arm Mount Stand',
        status: ShipmentStatus.delivered,
        scannedAt: now.subtract(const Duration(days: 2)),
        packedAt: now.subtract(const Duration(days: 2)),
        dispatchedAt: now.subtract(const Duration(days: 1)),
        completedAt: now.subtract(const Duration(hours: 4)),
        scannedBy: 'G.J. (Packer-Scanner)',
        lastModifiedAt: now,
      ),
      Shipment(
        trackingNumber: 'NVPH9918234710',
        recipientName: 'Roberto Tan',
        recipientPhone: '+63 922 888 6611',
        recipientAddress: '88 Malakas St, Pinyahan, Quezon City',
        platform: PlatformType.shopee,
        courier: CourierType.ninjaVan,
        weightKg: 0.60,
        declaredValue: 990.0,
        itemsSummary: 'Ultra-thin Laptop Cooling Pad',
        status: ShipmentStatus.returned,
        scannedAt: now.subtract(const Duration(days: 3)),
        completedAt: now.subtract(const Duration(hours: 1)),
        scannedBy: 'Nolan (Owner)',
        lastModifiedAt: now,
      ),
    ];

    _batches = [
      DispatchBatch(
        batchId: 'BATCH-INITIAL-01',
        batchNumber: 'BCH-J&T-948123',
        courier: CourierType.jAndT,
        driverName: 'Danilo Ramos',
        driverPlateNumber: 'NDB-4921',
        driverPhone: '0917-882-9901',
        handoverTimestamp: now.subtract(const Duration(hours: 1)),
        parcelCount: 1,
        totalWeightKg: 0.45,
        status: 'HANDED_OVER',
        notes: 'Midday pickup route completed',
        createdBy: 'Nolan (Owner)',
      ),
    ];

    _returns = [
      ReturnRecord(
        returnId: 'RTS-001',
        trackingNumber: 'NVPH9918234710',
        platform: PlatformType.shopee,
        courier: CourierType.ninjaVan,
        returnReason: ReturnReason.customerRejectedCod,
        itemCondition: ItemCondition.pristineResellable,
        refundStatus: RefundStatus.refundApproved,
        claimAmount: 990.0,
        notes: 'Customer refused COD delivery due to change of mind.',
        loggedBy: 'Nolan (Owner)',
        loggedAt: now.subtract(const Duration(hours: 1)),
      ),
    ];

    _logs = [
      AuditLog(
        id: _nextLogId++,
        action: 'SYSTEM_BOOT',
        details: 'Logistics engine initialized. Active user: Nolan (Owner)',
        performedBy: 'System',
        timestamp: now.subtract(const Duration(minutes: 30)),
      ),
      AuditLog(
        id: _nextLogId++,
        action: 'DISPATCH_HANDOVER',
        details: 'Handover batch BCH-J&T-948123 dispatched to rider Danilo Ramos',
        performedBy: 'Nolan (Owner)',
        timestamp: now.subtract(const Duration(hours: 1)),
      ),
    ];
  }
}
