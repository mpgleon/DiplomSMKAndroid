package com.example.diplomsmkandroid.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diplomsmkandroid.data.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity getById(int id);

    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> getAll();

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    List<UserEntity> getAllSync();

    @Query("SELECT COUNT(*) FROM users")
    int getCount();
}
