package com.example.diplomsmkandroid.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users",
        indices = {@Index(value = "email", unique = true)},
        foreignKeys = @ForeignKey(
                entity = FundEntity.class,
                parentColumns = "id",
                childColumns = "fund_id",
                onDelete = ForeignKey.SET_NULL))
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "role", defaultValue = "donor")
    public String role; // donor | volunteer | fund

    @ColumnInfo(name = "city")
    public String city;

    @ColumnInfo(name = "avatar_path")
    public String avatarPath;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "fund_id")
    public Integer fundId;
}
