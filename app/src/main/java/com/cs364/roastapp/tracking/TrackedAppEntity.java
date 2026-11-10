package com.cs364.roastapp.tracking;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tracked_app")
public class TrackedAppEntity {

    @PrimaryKey
    @NonNull
    public String packageName = "";
}
