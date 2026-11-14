package com.cs364.roastapp.core;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cs364.roastapp.focus.FocusSessionDao;
import com.cs364.roastapp.focus.FocusSessionEntity;
import com.cs364.roastapp.roast.CustomRoastDao;
import com.cs364.roastapp.roast.CustomRoastEntity;
import com.cs364.roastapp.tracking.TrackedAppDao;
import com.cs364.roastapp.tracking.TrackedAppEntity;

@Database(
        entities = {TrackedAppEntity.class, FocusSessionEntity.class, CustomRoastEntity.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "put_it_down.db";
    private static volatile AppDatabase instance;

    /** v3: tracked_app gains the per-app daily limit. */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE tracked_app ADD COLUMN limitMinutes INTEGER NOT NULL DEFAULT 30");
        }
    };

    public abstract TrackedAppDao trackedAppDao();

    public abstract FocusSessionDao focusSessionDao();

    public abstract CustomRoastDao customRoastDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addMigrations(MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }
}
