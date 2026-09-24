import 'enums.dart';

class Shipment {
  final String trackingNumber;
  final String recipientName;
  final String recipientPhone;
  final String recipientAddress;
  final PlatformType platform;
  final CourierType courier;
  final double weightKg;
  final String dimensionsCm;
  final double declaredValue;
  final String itemsSummary;
  final ShipmentStatus status;
  final String? batchId;
  final String notes;
  final DateTime scannedAt;
  final DateTime? packedAt;
  final DateTime? dispatchedAt;
  final DateTime? inTransitAt;
  final DateTime? completedAt;
  final String scannedBy;
  final SyncStatus syncStatus;
  final DateTime lastModifiedAt;

  const Shipment({
    required this.trackingNumber,
    required this.recipientName,
    required this.recipientPhone,
    required this.recipientAddress,
    required this.platform,
    required this.courier,
    required this.weightKg,
    this.dimensionsCm = '15x10x5',
    required this.declaredValue,
    required this.itemsSummary,
    this.status = ShipmentStatus.scanned,
    this.batchId,
    this.notes = '',
    required this.scannedAt,
    this.packedAt,
    this.dispatchedAt,
    this.inTransitAt,
    this.completedAt,
    this.scannedBy = 'Staff',
    this.syncStatus = SyncStatus.synced,
    required this.lastModifiedAt,
  });

  Shipment copyWith({
    String? trackingNumber,
    String? recipientName,
    String? recipientPhone,
    String? recipientAddress,
    PlatformType? platform,
    CourierType? courier,
    double? weightKg,
    String? dimensionsCm,
    double? declaredValue,
    String? itemsSummary,
    ShipmentStatus? status,
    String? batchId,
    String? notes,
    DateTime? scannedAt,
    DateTime? packedAt,
    DateTime? dispatchedAt,
    DateTime? inTransitAt,
    DateTime? completedAt,
    String? scannedBy,
    SyncStatus? syncStatus,
    DateTime? lastModifiedAt,
  }) {
    return Shipment(
      trackingNumber: trackingNumber ?? this.trackingNumber,
      recipientName: recipientName ?? this.recipientName,
      recipientPhone: recipientPhone ?? this.recipientPhone,
      recipientAddress: recipientAddress ?? this.recipientAddress,
      platform: platform ?? this.platform,
      courier: courier ?? this.courier,
      weightKg: weightKg ?? this.weightKg,
      dimensionsCm: dimensionsCm ?? this.dimensionsCm,
      declaredValue: declaredValue ?? this.declaredValue,
      itemsSummary: itemsSummary ?? this.itemsSummary,
      status: status ?? this.status,
      batchId: batchId ?? this.batchId,
      notes: notes ?? this.notes,
      scannedAt: scannedAt ?? this.scannedAt,
      packedAt: packedAt ?? this.packedAt,
      dispatchedAt: dispatchedAt ?? this.dispatchedAt,
      inTransitAt: inTransitAt ?? this.inTransitAt,
      completedAt: completedAt ?? this.completedAt,
      scannedBy: scannedBy ?? this.scannedBy,
      syncStatus: syncStatus ?? this.syncStatus,
      lastModifiedAt: lastModifiedAt ?? this.lastModifiedAt,
    );
  }
}
