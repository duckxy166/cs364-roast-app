package com.cs364.roastapp.tracking;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrackedAppDao {

    /** Oldest-added first, so editing a limit does not reorder the list. */
    @Query("SELECT * FROM tracked_app ORDER BY rowid")
    List<TrackedAppEntity> getAll();

    @Query("SELECT COUNT(*) FROM tracked_app")
    int count();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(TrackedAppEntity entity);

    @Query("UPDATE tracked_app SET limitMinutes = :minutes WHERE packageName = :packageName")
    int updateLimit(String packageName, long minutes);

    @Query("DELETE FROM tracked_app WHERE packageName = :packageName")
    void deleteByPackage(String packageName);
}
