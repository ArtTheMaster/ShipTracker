class AuditLog {
  final int id;
  final String action;
  final String details;
  final String performedBy;
  final DateTime timestamp;

  const AuditLog({
    required this.id,
    required this.action,
    required this.details,
    required this.performedBy,
    required this.timestamp,
  });
}
