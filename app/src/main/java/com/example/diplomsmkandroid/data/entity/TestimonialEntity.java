package com.example.diplomsmkandroid.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "testimonials",
        indices = {@Index("user_id")},
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.SET_NULL))
public class TestimonialEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "role")
    public String role;

    @ColumnInfo(name = "city")
    public String city;

    @ColumnInfo(name = "user_id")
    public Integer userId;
}
