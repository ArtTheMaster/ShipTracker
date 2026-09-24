import 'enums.dart';

class DispatchBatch {
  final String batchId;
  final String batchNumber;
  final CourierType courier;
  final String driverName;
  final String driverPlateNumber;
  final String driverPhone;
  final DateTime handoverTimestamp;
  final int parcelCount;
  final double totalWeightKg;
  final String status;
  final String notes;
  final String createdBy;
  final SyncStatus syncStatus;

  const DispatchBatch({
    required this.batchId,
    required this.batchNumber,
    required this.courier,
    required this.driverName,
    required this.driverPlateNumber,
    required this.driverPhone,
    required this.handoverTimestamp,
    this.parcelCount = 0,
    this.totalWeightKg = 0.0,
    this.status = 'HANDED_OVER',
    this.notes = '',
    this.createdBy = 'Staff',
    this.syncStatus = SyncStatus.synced,
  });
}
