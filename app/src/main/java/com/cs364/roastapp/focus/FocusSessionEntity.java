package com.cs364.roastapp.focus;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_session")
public class FocusSessionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long durationMinutes;

    public long completedAtMillis;

    public boolean completed;

    public FocusSessionEntity() {
    }

    @Ignore
    public FocusSessionEntity(long durationMinutes, long completedAtMillis, boolean completed) {
        this.durationMinutes = durationMinutes;
        this.completedAtMillis = completedAtMillis;
        this.completed = completed;
    }
}
