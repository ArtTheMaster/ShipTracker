import 'package:flutter/material.dart';
import '../services/logistics_service.dart';
import '../models/user_account.dart';
import '../models/enums.dart';

class AccountScreen extends StatelessWidget {
  final LogisticsService service;

  const AccountScreen({super.key, required this.service});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final currentUser = service.currentUser;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Staff & RBAC Security Profile', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Current User Profile Card
          Card(
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
              side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  CircleAvatar(
                    radius: 30,
                    backgroundColor: currentUser.avatarColor,
                    child: Text(
                      currentUser.fullName.substring(0, 1),
                      style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold),
                    ),
                  ),
                  const SizedBox(height: 10),
                  Text(currentUser.fullName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  const SizedBox(height: 2),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 2),
                    decoration: BoxDecoration(
                      color: currentUser.role == UserRole.ownerAdmin ? Colors.blue.shade50 : Colors.teal.shade50,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      currentUser.role.displayName,
                      style: TextStyle(
                        color: currentUser.role == UserRole.ownerAdmin ? Colors.blue.shade900 : Colors.teal.shade900,
                        fontWeight: FontWeight.bold,
                        fontSize: 11,
                      ),
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(currentUser.email, style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant)),
                  Text('Staff Code: ${currentUser.staffCode}', style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant)),
                  const SizedBox(height: 12),
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: theme.colorScheme.surfaceVariant,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      'Access Scope: ${currentUser.role.description}',
                      style: const TextStyle(fontSize: 11),
                      textAlign: TextAlign.center,
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Switch Account Demonstration
          Text('Switch Active Account (RBAC Demo)', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          ...UserAccount.allAccounts.map((account) {
            final isCurrent = account.id == currentUser.id;
            return Card(
              elevation: 0,
              margin: const EdgeInsets.only(bottom: 8),
              color: isCurrent ? theme.colorScheme.primaryContainer : theme.colorScheme.surface,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10),
                side: BorderSide(color: isCurrent ? theme.colorScheme.primary : theme.colorScheme.outline.withOpacity(0.3)),
              ),
              child: ListTile(
                onTap: () {
                  service.switchAccount(account);
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Switched to ${account.fullName}')),
                  );
                },
                leading: CircleAvatar(
                  backgroundColor: account.avatarColor,
                  child: Text(account.fullName.substring(0, 1), style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                ),
                title: Text(account.fullName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                subtitle: Text('${account.role.displayName} • ${account.email}', style: const TextStyle(fontSize: 11)),
                trailing: isCurrent ? const Icon(Icons.check_circle, color: Colors.green) : const Icon(Icons.chevron_right),
              ),
            );
          }),
          const SizedBox(height: 16),

          // RBAC Matrix Table
          Card(
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
              side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
            ),
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Role-Based Access Control (RBAC) Matrix', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                  const SizedBox(height: 10),
                  _buildMatrixHeader(),
                  const Divider(height: 12),
                  _buildMatrixRow('Barcode Scanner Intake', true, true),
                  _buildMatrixRow('Status Update & Lifecycle', true, true),
                  _buildMatrixRow('Dispatch Batch Manifest', true, true),
                  _buildMatrixRow('Log Return-To-Sender (RTS)', true, true),
                  _buildMatrixRow('View Gross Financials', true, false),
                  _buildMatrixRow('Permanent Record Deletion', true, false),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMatrixHeader() {
    return const Row(
      children: [
        Expanded(flex: 3, child: Text('Operational Capability', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11))),
        Expanded(child: Center(child: Text('Admin', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11)))),
        Expanded(child: Center(child: Text('Staff', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11)))),
      ],
    );
  }

  Widget _buildMatrixRow(String feature, bool adminAllowed, bool staffAllowed) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Expanded(flex: 3, child: Text(feature, style: const TextStyle(fontSize: 11))),
          Expanded(
            child: Center(
              child: Icon(
                adminAllowed ? Icons.check : Icons.close,
                size: 16,
                color: adminAllowed ? Colors.green : Colors.red,
              ),
            ),
          ),
          Expanded(
            child: Center(
              child: Icon(
                staffAllowed ? Icons.check : Icons.close,
                size: 16,
                color: staffAllowed ? Colors.green : Colors.red,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
