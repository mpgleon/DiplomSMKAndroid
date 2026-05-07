package com.example.diplomsmkandroid.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "volunteer_applications",
        indices = {@Index("user_id"), @Index("task_id")},
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class,
                        parentColumns = "id", childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = VolunteerTaskEntity.class,
                        parentColumns = "id", childColumns = "task_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class VolunteerApplicationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "task_id")
    public int taskId;

    @ColumnInfo(name = "status", defaultValue = "pending")
    public String status; // pending | accepted | rejected

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
