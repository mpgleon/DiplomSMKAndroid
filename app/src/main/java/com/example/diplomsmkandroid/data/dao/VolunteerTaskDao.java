package com.example.diplomsmkandroid.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diplomsmkandroid.data.entity.VolunteerTaskEntity;

import java.util.List;

@Dao
public interface VolunteerTaskDao {

    @Insert
    long insert(VolunteerTaskEntity task);

    @Insert
    void insertAll(List<VolunteerTaskEntity> tasks);

    @Query("SELECT * FROM volunteer_tasks")
    LiveData<List<VolunteerTaskEntity>> getAll();

    @Query("SELECT * FROM volunteer_tasks")
    List<VolunteerTaskEntity> getAllSync();

    @Query("SELECT * FROM volunteer_tasks LIMIT :limit OFFSET :offset")
    List<VolunteerTaskEntity> getPageSync(int limit, int offset);

    @Query("SELECT * FROM volunteer_tasks WHERE id = :id LIMIT 1")
    VolunteerTaskEntity getById(int id);

    @Query("SELECT * FROM volunteer_tasks WHERE fund_id = :fundId")
    List<VolunteerTaskEntity> getByFundSync(int fundId);

    @Query("SELECT COUNT(*) FROM volunteer_tasks")
    int getCount();

    @Update
    void update(VolunteerTaskEntity task);

    @Delete
    void delete(VolunteerTaskEntity task);

    @Query("UPDATE volunteer_tasks SET slots_taken = slots_taken + 1 WHERE id = :taskId")
    void incrementSlotsTaken(int taskId);

    @Query("SELECT COUNT(*) FROM volunteer_applications WHERE task_id = :taskId")
    int countApplicationsForTask(int taskId);

    @Query("UPDATE volunteer_tasks SET status = :status WHERE id = :id")
    void updateStatus(int id, String status);

    @Query("SELECT * FROM volunteer_tasks WHERE status = 'active'")
    List<VolunteerTaskEntity> getActiveSync();
}
