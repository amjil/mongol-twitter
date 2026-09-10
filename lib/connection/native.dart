import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

Future<String> getDatabasePath() async {
  final appDocsDir = await getApplicationDocumentsDirectory();
  final appDir = Directory(join(appDocsDir.path, "./"));
  if (!await appDir.exists()) {
    await appDir.create(recursive: true);
  }
  // 数据库命名为 mongol_zangila
  return join(appDir.path, "mongol_zangila.db");
}

/// Obtains a database connection for running drift in a Dart VM (iOS/Android/Desktop).
DatabaseConnection connect() {
  return DatabaseConnection.delayed(
    Future(() async {
      final dbPath = await getDatabasePath();
      print("Using native database at path $dbPath");
      return NativeDatabase.createBackgroundConnection(File(dbPath));
    }),
  );
}
