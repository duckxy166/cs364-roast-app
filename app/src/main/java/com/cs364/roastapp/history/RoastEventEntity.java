package com.cs364.roastapp.history;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "roast_event")
public class RoastEventEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String packageName;

    public String appLabel;

    public int level;

    public String message;

    public long timestampMillis;

    public RoastEventEntity() {
    }

    @Ignore
    public RoastEventEntity(String packageName, String appLabel, int level, String message, long timestampMillis) {
        this.packageName = packageName;
        this.appLabel = appLabel;
        this.level = level;
        this.message = message;
        this.timestampMillis = timestampMillis;
    }
}
