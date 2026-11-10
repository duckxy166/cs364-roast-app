package com.cs364.roastapp.focus;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FocusSessionDao {

    @Insert
    long insert(FocusSessionEntity session);

    @Query("SELECT * FROM focus_session ORDER BY completedAtMillis DESC")
    List<FocusSessionEntity> getAllSessions();

    @Query("SELECT COUNT(*) FROM focus_session WHERE completed = 1")
    int getCompletedCount();
}
