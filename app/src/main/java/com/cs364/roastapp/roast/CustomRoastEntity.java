package com.cs364.roastapp.roast;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_roast")
public class CustomRoastEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String text;

    public long createdAtMillis;

    public CustomRoastEntity() {
    }

    @Ignore
    public CustomRoastEntity(String text, long createdAtMillis) {
        this.text = text;
        this.createdAtMillis = createdAtMillis;
    }
}
