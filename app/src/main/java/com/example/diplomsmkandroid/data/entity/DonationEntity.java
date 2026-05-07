package com.example.diplomsmkandroid.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "donations",
        indices = {@Index("user_id"), @Index("project_id")},
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class,
                        parentColumns = "id", childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = ProjectEntity.class,
                        parentColumns = "id", childColumns = "project_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class DonationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "project_id")
    public int projectId;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
