package com.example.diplomsmkandroid.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "subscribers",
        indices = {@Index(value = "email", unique = true)})
public class SubscriberEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
