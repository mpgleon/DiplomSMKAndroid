package com.example.diplomsmkandroid.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects",
        indices = {@Index("fund_id")},
        foreignKeys = @ForeignKey(
                entity = FundEntity.class,
                parentColumns = "id",
                childColumns = "fund_id",
                onDelete = ForeignKey.CASCADE))
public class ProjectEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "region")
    public String region;

    @ColumnInfo(name = "goal", defaultValue = "0")
    public double goal;

    @ColumnInfo(name = "collected", defaultValue = "0")
    public double collected;

    @ColumnInfo(name = "days_left", defaultValue = "30")
    public int daysLeft;

    @ColumnInfo(name = "urgent", defaultValue = "0")
    public boolean urgent;

    @ColumnInfo(name = "fund_id")
    public Integer fundId;

    @ColumnInfo(name = "creator_id")
    public Integer creatorId;

    @ColumnInfo(name = "cover_path")
    public String coverPath;

    @ColumnInfo(name = "status", defaultValue = "active")
    public String status; // active | completed | archived

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public int getPercent() {
        if (goal <= 0) return 0;
        return (int) (collected / goal * 100);
    }
}
