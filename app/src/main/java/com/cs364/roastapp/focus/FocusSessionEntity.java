package com.cs364.roastapp.focus;

/** One row of the focus_session table: a finished focus timer. */
public class FocusSessionEntity {

    public long id;
    public long durationMinutes;
    public long completedAtMillis;
    public boolean completed;

    public FocusSessionEntity(long durationMinutes, long completedAtMillis, boolean completed) {
        this.durationMinutes = durationMinutes;
        this.completedAtMillis = completedAtMillis;
        this.completed = completed;
    }
}
