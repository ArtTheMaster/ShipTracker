import 'enums.dart';

class ReturnRecord {
  final String returnId;
  final String trackingNumber;
  final PlatformType platform;
  final CourierType courier;
  final ReturnReason returnReason;
  final ItemCondition itemCondition;
  final RefundStatus refundStatus;
  final double claimAmount;
  final String notes;
  final String loggedBy;
  final DateTime loggedAt;
  final SyncStatus syncStatus;

  const ReturnRecord({
    required this.returnId,
    required this.trackingNumber,
    required this.platform,
    required this.courier,
    required this.returnReason,
    required this.itemCondition,
    this.refundStatus = RefundStatus.pendingVerification,
    this.claimAmount = 0.0,
    this.notes = '',
    this.loggedBy = 'Staff',
    required this.loggedAt,
    this.syncStatus = SyncStatus.synced,
  });
}
