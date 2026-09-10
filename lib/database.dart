import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'package:sqlite3/sqlite3.dart';
import 'package:sqlite3_flutter_libs/sqlite3_flutter_libs.dart';

// ⚠️ WARNING: Generated file handled dynamically by Drift build_runner
part 'database.g.dart';

// =============================================================================
// 1. Table Definitions (Schema)
// =============================================================================

@DataClassName('OutboxEntry')
class OutboxEntries extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get mutationType => text()();
  TextColumn get payloadJson => text()();
  IntColumn get retryCount => integer().withDefault(const Constant(0))();

  // ⚡ Schema V4: Added global idempotency key for exact-once backend delivery
  TextColumn get idempotencyKey => text().nullable()();

  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
}

@DataClassName('Draft')
class Drafts extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get content => text()();
  TextColumn get mediaJson => text()();
  TextColumn get entitiesJson => text().nullable()();

  // ⚡ Schema V3: Added structured session context fields to preserve conversation history
  TextColumn get replyToId => text().nullable()();
  TextColumn get replyToName => text().nullable()();
  TextColumn get replyToAuthorId => text().nullable()();
  TextColumn get quoteId => text().nullable()();
  TextColumn get quoteAuthorName => text().nullable()();
  TextColumn get quotePostJson => text().nullable()();

  // ⚡ Schema V4: Preserve the session idempotency key even in draft state
  TextColumn get idempotencyKey => text().nullable()();

  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
}

@DataClassName('Message')
class Messages extends Table {
  TextColumn get id => text()();
  TextColumn get topic => text()();
  TextColumn get payloadJson => text()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {id};
}

// ⚡ Schema V5: 新增黑名单缓存表，用于离线状态下的双盲过滤
@DataClassName('BlockedUser')
class BlockedUsers extends Table {
  TextColumn get blockedUserId => text()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {blockedUserId};
}

// =============================================================================
// 2. Database Core & Connection
// =============================================================================

// ⚡ 必须在 tables 数组中注册 BlockedUsers
@DriftDatabase(tables: [OutboxEntries, Drafts, Messages, BlockedUsers])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  // ⚡ 升级到 V5
  @override
  int get schemaVersion => 5;

  @override
  MigrationStrategy get migration => MigrationStrategy(
    onCreate: (Migrator m) async {
      await m.createAll();
    },
    onUpgrade: (Migrator m, int from, int to) async {
      if (from < 2) {
        await m.addColumn(drafts, drafts.entitiesJson);
      }
      if (from < 3) {
        await m.addColumn(drafts, drafts.replyToId);
        await m.addColumn(drafts, drafts.replyToName);
        await m.addColumn(drafts, drafts.replyToAuthorId);
        await m.addColumn(drafts, drafts.quoteId);
        await m.addColumn(drafts, drafts.quoteAuthorName);
        await m.addColumn(drafts, drafts.quotePostJson);
      }
      if (from < 4) {
        await m.addColumn(outboxEntries, outboxEntries.idempotencyKey);
        await m.addColumn(drafts, drafts.idempotencyKey);
      }
      if (from < 5) {
        // ⚡ V5 迁移逻辑：创建新的 BlockedUsers 表
        await m.createTable(blockedUsers);
      }
    },
  );
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'mongol_zangila.sqlite'));

    // ⚡ FIXED: Removed the deprecated applyWorkaroundToOpenSqlite3OnOldAndroidVersions()

    final cachebase = (await getTemporaryDirectory()).path;
    sqlite3.tempDirectory = cachebase;

    return NativeDatabase.createInBackground(file);
  });
}

Future<AppDatabase> initDriftDatabase() async {
  return AppDatabase();
}
