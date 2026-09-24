import 'package:flutter/material.dart';

enum UserRole {
  ownerAdmin('Owner / Admin', 'Full control: analytics, reports, courier configs, staff management, returns'),
  staffPacker('Staff / Packer-Scanner', 'Operations: scanning, packing, dispatch batches, shipment status, return logging');

  final String displayName;
  final String description;

  const UserRole(this.displayName, this.description);

  bool get canDeleteShipments => this == UserRole.ownerAdmin;
  bool get canViewFinancialMetrics => this == UserRole.ownerAdmin;
  bool get canScanParcels => true;
  bool get canHandoverBatches => true;
}

enum ShipmentStatus {
  scanned('Scanned', 0),
  prepared('Prepared / Packed', 1),
  dispatched('Dispatched', 2),
  inTransit('In-Transit', 3),
  delivered('Delivered', 4),
  returned('Returned (RTS)', 5),
  cancelled('Cancelled', 6);

  final String displayName;
  final int stepIndex;

  const ShipmentStatus(this.displayName, this.stepIndex);

  bool get isTerminal =>
      this == ShipmentStatus.delivered ||
      this == ShipmentStatus.returned ||
      this == ShipmentStatus.cancelled;
}

enum PlatformType {
  shopee('Shopee', Color(0xFFEE4D2D)),
  lazada('Lazada', Color(0xFF0F146D)),
  tiktokShop('TikTok Shop', Color(0xFF111111)),
  other('Direct / Other', Color(0xFF475569));

  final String displayName;
  final Color badgeColor;

  const PlatformType(this.displayName, this.badgeColor);
}

enum CourierType {
  spx('Shopee Xpress (SPX)', Color(0xFFEE4D2D)),
  jAndT('J&T Express', Color(0xFFE30613)),
  flashExpress('Flash Express', Color(0xFFEAB308)),
  ninjaVan('Ninja Van', Color(0xFFC2002F)),
  lazadaLex('Lazada (LEX)', Color(0xFF0284C7)),
  other('Other Courier', Color(0xFF64748B));

  final String displayName;
  final Color brandColor;

  const CourierType(this.displayName, this.brandColor);
}

enum ReturnReason {
  customerRejectedCod('Customer Rejected / COD Refusal'),
  damagedInTransit('Damaged in Transit'),
  wrongItemSent('Wrong Item / Wrong Variation'),
  incompleteAddress('Address Incomplete / Unreachable'),
  fraudulentOrder('Suspected Fraud / Fake Order'),
  buyerCancelled('Buyer Cancelled Late'),
  other('Other Logistics Issue');

  final String label;
  const ReturnReason(this.label);
}

enum ItemCondition {
  pristineResellable('Pristine / Resellable (Grade A)'),
  damagedPackaging('Packaging Damaged / Item OK (Grade B)'),
  severelyDamaged('Severely Damaged / Total Loss (Grade C)');

  final String label;
  const ItemCondition(this.label);
}

enum RefundStatus {
  pendingVerification('Pending Inspection', Color(0xFFD97706)),
  refundApproved('Refund Approved', Color(0xFF059669)),
  disputeRaised('Dispute Raised with Platform', Color(0xFF7C3AED)),
  claimFiledCourier('Courier Claim Filed', Color(0xFF2563EB)),
  rejected('Refund Rejected', Color(0xFFDC2626));

  final String label;
  final Color color;
  const RefundStatus(this.label, this.color);
}

enum SyncStatus {
  synced('Synced', Color(0xFF059669)),
  pendingSync('Pending Sync', Color(0xFFD97706)),
  conflict('Conflict', Color(0xFFDC2626));

  final String label;
  final Color color;
  const SyncStatus(this.label, this.color);
}
