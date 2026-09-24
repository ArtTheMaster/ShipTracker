import 'package:flutter_test/flutter_test.dart';
import 'package:shiptracker_flutter/services/logistics_service.dart';
import 'package:shiptracker_flutter/models/enums.dart';

void main() {
  group('LogisticsService Business Logic Tests', () {
    test('Default user is Nolan with Admin role', () {
      final service = LogisticsService();
      expect(service.currentUser.fullName, 'Nolan (Owner)');
      expect(service.currentUser.role, UserRole.ownerAdmin);
      expect(service.currentUser.role.canDeleteShipments, true);
    });

    test('Staff packer cannot delete shipments', () {
      final service = LogisticsService();
      service.switchAccount(service.currentUser.role == UserRole.ownerAdmin
          ? service.currentUser
          : service.currentUser);
      expect(UserRole.staffPacker.canDeleteShipments, false);
      expect(UserRole.staffPacker.canViewFinancialMetrics, false);
      expect(UserRole.staffPacker.canScanParcels, true);
    });

    test('Barcode prefix auto-detects courier', () {
      final service = LogisticsService();
      final spxResult = service.detectCourierAndPlatform('SPXPH123456');
      expect(spxResult['courier'], CourierType.spx);
      expect(spxResult['platform'], PlatformType.shopee);

      final lexResult = service.detectCourierAndPlatform('MP9921LEX');
      expect(lexResult['courier'], CourierType.lazadaLex);
      expect(lexResult['platform'], PlatformType.lazada);

      final jtResult = service.detectCourierAndPlatform('JNET991048');
      expect(jtResult['courier'], CourierType.jAndT);
      expect(jtResult['platform'], PlatformType.tiktokShop);
    });

    test('Registering new parcel updates outbound list', () {
      final service = LogisticsService();
      final initialCount = service.shipments.length;

      service.addShipment(
        trackingNumber: 'TEST-TRACKING-101',
        recipientName: 'Test Customer',
        recipientPhone: '0917-000-0000',
        recipientAddress: '123 Test St, Makati City',
        platform: PlatformType.shopee,
        courier: CourierType.spx,
        weightKg: 1.0,
        declaredValue: 500.0,
        itemsSummary: 'Test Package Items',
      );

      expect(service.shipments.length, initialCount + 1);
      expect(service.shipments.first.trackingNumber, 'TEST-TRACKING-101');
      expect(service.shipments.first.status, ShipmentStatus.scanned);
    });

    test('Creating dispatch batch transitions parcels to Dispatched', () {
      final service = LogisticsService();
      final targetBarcode = service.shipments.first.trackingNumber;

      final batch = service.createDispatchBatch(
        courier: CourierType.jAndT,
        driverName: 'Test Driver',
        driverPlateNumber: 'TEST-123',
        driverPhone: '0918-000-0000',
        trackingNumbers: [targetBarcode],
      );

      expect(batch.driverName, 'Test Driver');
      final updated = service.shipments.firstWhere((s) => s.trackingNumber == targetBarcode);
      expect(updated.status, ShipmentStatus.dispatched);
      expect(updated.batchId, batch.batchId);
    });
  });
}
