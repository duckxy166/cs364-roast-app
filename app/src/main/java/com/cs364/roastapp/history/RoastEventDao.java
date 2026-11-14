package com.cs364.roastapp.history;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RoastEventDao {

    @Insert
    long insert(RoastEventEntity event);

    @Query("SELECT * FROM roast_event ORDER BY timestampMillis DESC, id DESC")
    List<RoastEventEntity> getAllEvents();

    @Delete
    void delete(RoastEventEntity event);

    @Query("DELETE FROM roast_event")
    void deleteAll();
}
