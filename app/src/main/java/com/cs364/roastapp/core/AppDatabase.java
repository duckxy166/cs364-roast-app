package com.cs364.roastapp.core;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cs364.roastapp.focus.FocusSessionDao;
import com.cs364.roastapp.focus.FocusSessionEntity;
import com.cs364.roastapp.roast.CustomRoastDao;
import com.cs364.roastapp.roast.CustomRoastEntity;
import com.cs364.roastapp.tracking.TrackedAppDao;
import com.cs364.roastapp.tracking.TrackedAppEntity;

@Database(
        entities = {TrackedAppEntity.class, FocusSessionEntity.class, CustomRoastEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "put_it_down.db";
    private static volatile AppDatabase instance;

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
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }
}
