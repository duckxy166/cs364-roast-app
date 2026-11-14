package com.cs364.roastapp.tracking;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tracked_app")
public class TrackedAppEntity {

    public static final long DEFAULT_LIMIT_MINUTES = 30L;

    @PrimaryKey
    @NonNull
    public String packageName = "";

    /** Daily limit in minutes (minimum 1). */
    @ColumnInfo(defaultValue = "30")
    public long limitMinutes = DEFAULT_LIMIT_MINUTES;
}
