package com.cs364.roastapp.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cs364.roastapp.focus.FocusSessionEntity;
import com.cs364.roastapp.history.RoastEventEntity;
import com.cs364.roastapp.tracking.TrackedAppEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the SQLite tables of the app and holds every Insert / Query / Update / Delete.
 *
 * Every value that comes from outside is passed through ContentValues or through
 * "column=?" with a String[] argument, never joined into the SQL text, so a value
 * cannot change the command (SQL Injection prevention).
 */
public class AppDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "put_it_down.db";
    private static final int DATABASE_VERSION = 5;

    // Table tracked_app: apps the user chose to limit
    private static final String TABLE_TRACKED_APP = "tracked_app";
    private static final String COL_APP_PACKAGE = "packageName";
    private static final String COL_APP_LIMIT = "limitMinutes";

    // Table focus_session: finished focus timers
    private static final String TABLE_FOCUS_SESSION = "focus_session";
    private static final String COL_FOCUS_ID = "id";
    private static final String COL_FOCUS_DURATION = "durationMinutes";
    private static final String COL_FOCUS_COMPLETED_AT = "completedAtMillis";
    private static final String COL_FOCUS_COMPLETED = "completed";

    // Table roast_event: history of reminders that were shown
    private static final String TABLE_ROAST_EVENT = "roast_event";
    private static final String COL_EVENT_ID = "id";
    private static final String COL_EVENT_PACKAGE = "packageName";
    private static final String COL_EVENT_APP_LABEL = "appLabel";
    private static final String COL_EVENT_LEVEL = "level";
    private static final String COL_EVENT_MESSAGE = "message";
    private static final String COL_EVENT_TIMESTAMP = "timestampMillis";

    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Version 1 has no upgrade path: start again with empty tables.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKED_APP);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOCUS_SESSION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROAST_EVENT);
            createTables(db);
            return;
        }
        if (oldVersion < 3) {
            // Version 3: each tracked app gets its own daily limit.
            db.execSQL("ALTER TABLE " + TABLE_TRACKED_APP + " ADD COLUMN "
                    + COL_APP_LIMIT + " INTEGER NOT NULL DEFAULT 30");
        }
        if (oldVersion < 5) {
            // Version 5: user-written reminder lines were removed.
            db.execSQL("DROP TABLE IF EXISTS custom_roast");
        }
        // Version 4 added roast_event; create any table that is still missing.
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TRACKED_APP + " ("
                + COL_APP_PACKAGE + " TEXT NOT NULL PRIMARY KEY, "
                + COL_APP_LIMIT + " INTEGER NOT NULL DEFAULT 30)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FOCUS_SESSION + " ("
                + COL_FOCUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + COL_FOCUS_DURATION + " INTEGER NOT NULL, "
                + COL_FOCUS_COMPLETED_AT + " INTEGER NOT NULL, "
                + COL_FOCUS_COMPLETED + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ROAST_EVENT + " ("
                + COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + COL_EVENT_PACKAGE + " TEXT, "
                + COL_EVENT_APP_LABEL + " TEXT, "
                + COL_EVENT_LEVEL + " INTEGER NOT NULL, "
                + COL_EVENT_MESSAGE + " TEXT, "
                + COL_EVENT_TIMESTAMP + " INTEGER NOT NULL)");
    }

    // ---------- tracked_app ----------

    /** Oldest-added first, so editing a limit does not reorder the list. */
    public List<TrackedAppEntity> getAllTrackedApps() {
        List<TrackedAppEntity> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_TRACKED_APP, null, null, null,
                null, null, "rowid")) {
            while (c.moveToNext()) {
                TrackedAppEntity item = new TrackedAppEntity();
                item.packageName = c.getString(c.getColumnIndexOrThrow(COL_APP_PACKAGE));
                item.limitMinutes = c.getLong(c.getColumnIndexOrThrow(COL_APP_LIMIT));
                items.add(item);
            }
        }
        return items;
    }

    public int countTrackedApps() {
        return (int) DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_TRACKED_APP);
    }

    /** Returns the new row id, or -1 when the app is already in the table. */
    public long insertTrackedApp(String packageName, long limitMinutes) {
        ContentValues values = new ContentValues();
        values.put(COL_APP_PACKAGE, packageName);
        values.put(COL_APP_LIMIT, limitMinutes);
        return getWritableDatabase().insertWithOnConflict(TABLE_TRACKED_APP, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Returns the number of rows changed (0 when the app is not tracked yet). */
    public int updateTrackedAppLimit(String packageName, long limitMinutes) {
        ContentValues values = new ContentValues();
        values.put(COL_APP_LIMIT, limitMinutes);
        return getWritableDatabase().update(TABLE_TRACKED_APP, values,
                COL_APP_PACKAGE + "=?", new String[]{packageName});
    }

    public int deleteTrackedApp(String packageName) {
        return getWritableDatabase().delete(TABLE_TRACKED_APP,
                COL_APP_PACKAGE + "=?", new String[]{packageName});
    }

    // ---------- focus_session ----------

    public long insertFocusSession(long durationMinutes, long completedAtMillis, boolean completed) {
        ContentValues values = new ContentValues();
        values.put(COL_FOCUS_DURATION, durationMinutes);
        values.put(COL_FOCUS_COMPLETED_AT, completedAtMillis);
        values.put(COL_FOCUS_COMPLETED, completed ? 1 : 0);
        return getWritableDatabase().insert(TABLE_FOCUS_SESSION, null, values);
    }

    /** Newest first. */
    public List<FocusSessionEntity> getAllFocusSessions() {
        List<FocusSessionEntity> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_FOCUS_SESSION, null, null, null,
                null, null, COL_FOCUS_COMPLETED_AT + " DESC")) {
            while (c.moveToNext()) {
                FocusSessionEntity item = new FocusSessionEntity(
                        c.getLong(c.getColumnIndexOrThrow(COL_FOCUS_DURATION)),
                        c.getLong(c.getColumnIndexOrThrow(COL_FOCUS_COMPLETED_AT)),
                        c.getInt(c.getColumnIndexOrThrow(COL_FOCUS_COMPLETED)) == 1);
                item.id = c.getLong(c.getColumnIndexOrThrow(COL_FOCUS_ID));
                items.add(item);
            }
        }
        return items;
    }

    public int countCompletedFocusSessions() {
        return (int) DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_FOCUS_SESSION,
                COL_FOCUS_COMPLETED + "=?", new String[]{"1"});
    }

    // ---------- roast_event ----------

    public long insertRoastEvent(String packageName, String appLabel, int level,
                                 String message, long timestampMillis) {
        ContentValues values = new ContentValues();
        values.put(COL_EVENT_PACKAGE, packageName);
        values.put(COL_EVENT_APP_LABEL, appLabel);
        values.put(COL_EVENT_LEVEL, level);
        values.put(COL_EVENT_MESSAGE, message);
        values.put(COL_EVENT_TIMESTAMP, timestampMillis);
        return getWritableDatabase().insert(TABLE_ROAST_EVENT, null, values);
    }

    /** Newest first. */
    public List<RoastEventEntity> getAllRoastEvents() {
        List<RoastEventEntity> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_ROAST_EVENT, null, null, null,
                null, null, COL_EVENT_TIMESTAMP + " DESC, " + COL_EVENT_ID + " DESC")) {
            while (c.moveToNext()) {
                RoastEventEntity item = new RoastEventEntity(
                        c.getString(c.getColumnIndexOrThrow(COL_EVENT_PACKAGE)),
                        c.getString(c.getColumnIndexOrThrow(COL_EVENT_APP_LABEL)),
                        c.getInt(c.getColumnIndexOrThrow(COL_EVENT_LEVEL)),
                        c.getString(c.getColumnIndexOrThrow(COL_EVENT_MESSAGE)),
                        c.getLong(c.getColumnIndexOrThrow(COL_EVENT_TIMESTAMP)));
                item.id = c.getLong(c.getColumnIndexOrThrow(COL_EVENT_ID));
                items.add(item);
            }
        }
        return items;
    }

    public int deleteRoastEvent(long id) {
        return getWritableDatabase().delete(TABLE_ROAST_EVENT,
                COL_EVENT_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteAllRoastEvents() {
        return getWritableDatabase().delete(TABLE_ROAST_EVENT, null, null);
    }
}
