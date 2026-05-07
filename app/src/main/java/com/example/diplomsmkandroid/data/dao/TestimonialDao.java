package com.example.diplomsmkandroid.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.diplomsmkandroid.data.entity.TestimonialEntity;

import java.util.List;

@Dao
public interface TestimonialDao {

    @Insert
    long insert(TestimonialEntity testimonial);

    @Insert
    void insertAll(List<TestimonialEntity> testimonials);

    @Query("SELECT * FROM testimonials")
    LiveData<List<TestimonialEntity>> getAll();

    @Query("SELECT * FROM testimonials")
    List<TestimonialEntity> getAllSync();
}
