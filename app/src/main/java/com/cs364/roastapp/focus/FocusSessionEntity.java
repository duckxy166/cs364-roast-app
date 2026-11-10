package com.cs364.roastapp.focus;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_session")
public class FocusSessionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
}
