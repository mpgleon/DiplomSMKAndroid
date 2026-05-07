package com.example.diplomsmkandroid.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.diplomsmkandroid.data.entity.ReviewEntity;

import java.util.List;

@Dao
public interface ReviewDao {

    @Insert
    long insert(ReviewEntity review);

    @Query("SELECT * FROM reviews WHERE project_id = :projectId ORDER BY created_at DESC")
    LiveData<List<ReviewEntity>> getByProject(int projectId);

    @Query("SELECT * FROM reviews WHERE project_id = :projectId ORDER BY created_at DESC")
    List<ReviewEntity> getByProjectSync(int projectId);

    @Query("SELECT * FROM reviews WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<ReviewEntity>> getByUser(int userId);

    @Query("SELECT * FROM reviews WHERE user_id = :userId ORDER BY created_at DESC")
    List<ReviewEntity> getByUserSync(int userId);

    @Query("SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE project_id = :projectId")
    double getAvgRating(int projectId);

    @Query("SELECT COUNT(*) FROM reviews WHERE project_id = :projectId")
    int getCountForProject(int projectId);
}
