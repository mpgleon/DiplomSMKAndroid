package com.example.diplomsmkandroid.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.diplomsmkandroid.data.entity.SubscriberEntity;

@Dao
public interface SubscriberDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(SubscriberEntity subscriber);

    @Query("SELECT COUNT(*) FROM subscribers WHERE email = :email")
    int countByEmail(String email);
}
