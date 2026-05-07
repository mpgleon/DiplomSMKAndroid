package com.example.diplomsmkandroid.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "funds")
public class FundEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "city")
    public String city;

    @ColumnInfo(name = "founded_year")
    public int foundedYear;

    @ColumnInfo(name = "verified", defaultValue = "1")
    public boolean verified;

    @ColumnInfo(name = "people_helped", defaultValue = "0")
    public int peopleHelped;

    @ColumnInfo(name = "people_helped_label", defaultValue = "людей")
    public String peopleHelpedLabel;

    @ColumnInfo(name = "volunteers_count", defaultValue = "0")
    public int volunteersCount;
}
