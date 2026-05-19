package com.example.diplomsmkandroid.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diplomsmkandroid.data.entity.ProjectEntity;

import java.util.List;

@Dao
public interface ProjectDao {

    @Insert
    long insert(ProjectEntity project);

    @Update
    void update(ProjectEntity project);

    @Query("SELECT * FROM projects WHERE status = 'active' ORDER BY created_at DESC")
    LiveData<List<ProjectEntity>> getActive();

    @Query("SELECT * FROM projects WHERE status = 'active' ORDER BY created_at DESC")
    List<ProjectEntity> getActiveSync();

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    ProjectEntity getById(int id);

    @Query("SELECT * FROM projects WHERE fund_id = :fundId")
    LiveData<List<ProjectEntity>> getByFund(int fundId);

    @Query("SELECT * FROM projects WHERE fund_id = :fundId")
    List<ProjectEntity> getByFundSync(int fundId);

    @Query("SELECT COUNT(*) FROM projects WHERE status = 'active'")
    int getActiveCount();

    @Query("SELECT COUNT(*) FROM projects")
    int getCount();

    @Query("SELECT * FROM projects WHERE status = 'active' AND category = :category ORDER BY created_at DESC")
    LiveData<List<ProjectEntity>> getByCategory(String category);

    @Query("SELECT * FROM projects WHERE status = 'active' ORDER BY collected DESC")
    LiveData<List<ProjectEntity>> getActiveSortedByPopularity();

    @Query("SELECT * FROM projects WHERE status = 'active' ORDER BY created_at DESC")
    LiveData<List<ProjectEntity>> getActiveSortedByDate();

    @Query("SELECT * FROM projects WHERE status = 'active' ORDER BY goal DESC")
    LiveData<List<ProjectEntity>> getActiveSortedByGoal();

    @Query("SELECT * FROM projects WHERE status = 'active' ORDER BY urgent DESC, days_left ASC")
    LiveData<List<ProjectEntity>> getActiveSortedByUrgency();

    @Query("SELECT * FROM projects ORDER BY created_at DESC")
    List<ProjectEntity> getAllSync();

    @Query("SELECT * FROM projects ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<ProjectEntity> getPageSync(int limit, int offset);

    @Query("SELECT * FROM projects WHERE creator_id = :creatorId ORDER BY created_at DESC")
    List<ProjectEntity> getByCreatorSync(int creatorId);

    @Query("SELECT COUNT(*) FROM projects WHERE LOWER(title) = LOWER(:title)")
    int countByTitle(String title);

    @Delete
    void delete(ProjectEntity project);
}
