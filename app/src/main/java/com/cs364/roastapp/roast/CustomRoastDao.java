package com.cs364.roastapp.roast;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CustomRoastDao {

    @Insert
    long insert(CustomRoastEntity item);

    @Query("SELECT * FROM custom_roast ORDER BY createdAtMillis DESC")
    List<CustomRoastEntity> getAll();

    @Delete
    void delete(CustomRoastEntity item);
}
