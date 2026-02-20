import 'package:drift/drift.dart';
import 'connection/connection.dart';

part 'database.g.dart';

// 1. Users table: Quick lookup for nicknames in lists or avatars
class Users extends Table {
  TextColumn get id => text()();
  TextColumn get nickname => text().nullable()();
  TextColumn get avatarUrl => text().nullable()();

  @override
  Set<Column> get primaryKey => {id};
}

// 2. Messages table: Core chat records
class Messages extends Table {
  IntColumn get localId => integer().autoIncrement()(); // Local primary key
  TextColumn get id => text().nullable()();             // Server-returned UUID
  TextColumn get senderId => text()();
  TextColumn get receiverId => text()();
  TextColumn get content => text()();                   // Mongolian content
  TextColumn get insertedAt => text()();                // ISO8601 timestamp string
  
  // Offline sync flag
  BoolColumn get isSynced => boolean().withDefault(const Constant(false))();

  // Type extension: 'text', 'image', 'voice'
  TextColumn get type => text().withDefault(const Constant('text'))(); 
}

// 3. PendingActions table: Offline queue for chat (retry send when back online)
class PendingActions extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get actionType => text()();
  TextColumn get payload => text()();
}

@DriftDatabase(tables: [Users, Messages, PendingActions])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(connect());

  @override
  int get schemaVersion => 3; // Schema version number

  @override
  MigrationStrategy get migration => MigrationStrategy(
    onCreate: (m) => m.createAll(),
    onUpgrade: (m, from, to) async {
      if (from < 2) {
        await m.createTable(messages);
      }
      if (from < 3) {
        await m.createTable(pendingActions);
      }
    },
  );
}

// Database initialization function
Future<AppDatabase> initDriftDatabase() async {
  final db = AppDatabase();
  // Simple validation
  await db.customSelect("SELECT 1").get();
  return db;
}