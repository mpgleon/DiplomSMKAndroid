package com.example.diplomsmkandroid.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diplomsmkandroid.data.entity.FundEntity;

import java.util.List;

@Dao
public interface FundDao {

    @Insert
    long insert(FundEntity fund);

    @Insert
    void insertAll(List<FundEntity> funds);

    @Query("SELECT * FROM funds")
    LiveData<List<FundEntity>> getAll();

    @Query("SELECT * FROM funds")
    List<FundEntity> getAllSync();

    @Query("SELECT * FROM funds LIMIT :limit OFFSET :offset")
    List<FundEntity> getPageSync(int limit, int offset);

    @Query("SELECT * FROM funds WHERE id = :id LIMIT 1")
    FundEntity getById(int id);

    @Update
    void update(FundEntity fund);

    @Query("SELECT COUNT(*) FROM funds")
    int getCount();
}
