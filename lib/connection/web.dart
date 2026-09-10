import 'dart:async';
import 'package:drift/drift.dart';
import 'package:drift/wasm.dart';
import 'package:flutter/foundation.dart';

const webDbName = "mongol_zangila_web";

final _sqlite3Uri = Uri.parse('/sqlite3.wasm');
final _driftWorkerUri = Uri.parse('/drift_worker.js');

/// Obtains a database connection for running drift on the web.
DatabaseConnection connect() {
  return DatabaseConnection.delayed(
    Future(() async {
      final db = await WasmDatabase.open(
        databaseName: webDbName,
        sqlite3Uri: _sqlite3Uri,
        driftWorkerUri: _driftWorkerUri,
      );

      if (db.missingFeatures.isNotEmpty) {
        debugPrint(
          'Using ${db.chosenImplementation} due to unsupported '
          'browser features: ${db.missingFeatures}',
        );
      }

      print("Chosen Web sqlite implementation: ${db.chosenImplementation}");
      return db.resolvedExecutor;
    }),
  );
}
