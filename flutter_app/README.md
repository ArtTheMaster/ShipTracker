# GJandAsher Outbound Logistics - Flutter & Dart Edition

This folder contains the complete **Flutter & Dart** implementation of the Outbound Logistics & Dispatch Tracker application.

## Prerequisites for VS Code

1. **Flutter SDK**:
   - Download and install the Flutter SDK from [flutter.dev](https://docs.flutter.dev/get-started/install).
   - Ensure `flutter` is added to your system PATH (`flutter doctor` in terminal).
2. **Visual Studio Code Extensions**:
   - Install the **Flutter** extension (by Dart Code / flutter.dev).
   - Install the **Dart** extension.

## Running & Testing in Visual Studio Code

### 1. Open the Flutter project
In VS Code, choose **File > Open Folder...** and select the `flutter_app` folder (or open the root repository and navigate to `flutter_app` in the integrated terminal).

### 2. Install dependencies
Open the integrated terminal in VS Code (`Ctrl + \`` or `Cmd + \``) and run:
```bash
cd flutter_app
flutter pub get
```

### 3. Run Automated Tests
To test the business logic, RBAC, barcode recognition, and dispatch batching:
```bash
flutter test
```

### 4. Run the App
Launch on an Android emulator, connected phone, or Chrome:
```bash
# Run on default connected device / emulator
flutter run

# Or run directly in Chrome for instant testing
flutter run -d chrome
```

## Architecture & Code Structure

- `lib/main.dart`: App entry point with Material 3 theming.
- `lib/models/`: Dart enums & domain data models (`enums.dart`, `shipment.dart`, `dispatch_batch.dart`, `return_record.dart`, `user_account.dart`, `audit_log.dart`).
- `lib/services/logistics_service.dart`: Centralized `ChangeNotifier` state engine handling parcel intake, courier auto-detect regex rules, dispatch batching, RTS logging, RBAC enforcement, and offline sync queuing.
- `lib/screens/`:
  - `dashboard_screen.dart`: Outbound KPIs, declared value, recent audit logs.
  - `scanner_screen.dart`: Waybill barcode reader, live courier pattern matching, recipient intake.
  - `shipments_screen.dart`: Searchable, filterable parcel registry with status chips & details bottom sheet.
  - `dispatch_screen.dart`: Courier handover batches & text manifest generator for rider sign-off.
  - `returns_screen.dart`: RTS logging, damage condition classification & claims.
  - `reports_screen.dart`: Executive analytics, courier volume breakdown & raw CSV export.
  - `sync_screen.dart`: Network status simulation toggle & pending offline queue.
  - `account_screen.dart`: Active role switching (Nolan Admin vs G.J. Staff) & RBAC capability matrix.
- `test/logistics_test.dart`: Unit tests covering intake, auto-detection, and dispatch.
