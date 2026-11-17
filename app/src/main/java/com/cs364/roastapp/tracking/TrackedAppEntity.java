package com.cs364.roastapp.tracking;

/** One row of the tracked_app table: an app the user chose to limit. */
public class TrackedAppEntity {

    public static final long DEFAULT_LIMIT_MINUTES = 30L;

    public String packageName = "";

    /** Daily limit in minutes (minimum 1). */
    public long limitMinutes = DEFAULT_LIMIT_MINUTES;
}
