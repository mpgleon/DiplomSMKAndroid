package com.example.diplomsmkandroid.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "volunteer_tasks",
        indices = {@Index("fund_id")},
        foreignKeys = @ForeignKey(
                entity = FundEntity.class,
                parentColumns = "id",
                childColumns = "fund_id",
                onDelete = ForeignKey.CASCADE))
public class VolunteerTaskEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "city")
    public String city;

    @ColumnInfo(name = "schedule")
    public String schedule;

    @ColumnInfo(name = "slots_total", defaultValue = "0")
    public int slotsTotal;

    @ColumnInfo(name = "slots_taken", defaultValue = "0")
    public int slotsTaken;

    @ColumnInfo(name = "rating_plus", defaultValue = "0")
    public int ratingPlus;

    @ColumnInfo(name = "fund_id")
    public Integer fundId;

    @ColumnInfo(name = "status", defaultValue = "pending")
    public String status; // pending | active | rejected
}
