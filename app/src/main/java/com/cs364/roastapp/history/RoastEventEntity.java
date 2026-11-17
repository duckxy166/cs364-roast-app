package com.cs364.roastapp.history;

/** One row of the roast_event table: a reminder that was shown to the user. */
public class RoastEventEntity {

    public long id;
    public String packageName;
    public String appLabel;
    public int level;
    public String message;
    public long timestampMillis;

    public RoastEventEntity(String packageName, String appLabel, int level, String message, long timestampMillis) {
        this.packageName = packageName;
        this.appLabel = appLabel;
        this.level = level;
        this.message = message;
        this.timestampMillis = timestampMillis;
    }
}
