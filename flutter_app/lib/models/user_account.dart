import 'package:flutter/material.dart';
import 'enums.dart';

class UserAccount {
  final String id;
  final String fullName;
  final UserRole role;
  final String email;
  final String staffCode;
  final Color avatarColor;

  const UserAccount({
    required this.id,
    required this.fullName,
    required this.role,
    required this.email,
    required this.staffCode,
    required this.avatarColor,
  });

  static const defaultAdmin = UserAccount(
    id: 'USR-001',
    fullName: 'Nolan (Owner)',
    role: UserRole.ownerAdmin,
    email: 'nolan.admin@gjandashershiptracker.com',
    staffCode: 'ADM-01',
    avatarColor: Color(0xFF1E3A8A),
  );

  static const defaultStaff = UserAccount(
    id: 'USR-002',
    fullName: 'G.J. (Packer-Scanner)',
    role: UserRole.staffPacker,
    email: 'gj.scanner@gjandashershiptracker.com',
    staffCode: 'OPR-04',
    avatarColor: Color(0xFFF97316),
  );

  static const allAccounts = [
    defaultAdmin,
    defaultStaff,
    UserAccount(
      id: 'USR-003',
      fullName: 'Maria Santos (Fulfillment Staff)',
      role: UserRole.staffPacker,
      email: 'maria.s@gjandashershiptracker.com',
      staffCode: 'OPR-07',
      avatarColor: Color(0xFF0D9488),
    ),
  ];
}
