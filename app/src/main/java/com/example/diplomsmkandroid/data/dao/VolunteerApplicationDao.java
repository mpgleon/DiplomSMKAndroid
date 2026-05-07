package com.example.diplomsmkandroid.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.diplomsmkandroid.data.entity.VolunteerApplicationEntity;

import java.util.List;

@Dao
public interface VolunteerApplicationDao {

    @Insert
    long insert(VolunteerApplicationEntity application);

    @Query("SELECT * FROM volunteer_applications WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<VolunteerApplicationEntity>> getByUser(int userId);

    @Query("SELECT * FROM volunteer_applications WHERE user_id = :userId ORDER BY created_at DESC")
    List<VolunteerApplicationEntity> getByUserSync(int userId);

    @Query("SELECT * FROM volunteer_applications WHERE user_id = :userId AND task_id = :taskId LIMIT 1")
    VolunteerApplicationEntity getByUserAndTask(int userId, int taskId);

    @Query("SELECT COUNT(*) FROM volunteer_applications WHERE user_id = :userId AND task_id = :taskId")
    int countByUserAndTask(int userId, int taskId);

    @Query("SELECT va.* FROM volunteer_applications va " +
            "INNER JOIN volunteer_tasks vt ON va.task_id = vt.id " +
            "WHERE vt.fund_id = :fundId ORDER BY va.created_at DESC")
    List<VolunteerApplicationEntity> getByFundSync(int fundId);

    @Query("UPDATE volunteer_applications SET status = :status WHERE id = :id")
    void updateStatus(int id, String status);

    @Query("SELECT * FROM volunteer_applications WHERE id = :id LIMIT 1")
    VolunteerApplicationEntity getById(int id);

    @Query("SELECT COUNT(*) FROM volunteer_applications WHERE task_id = :taskId")
    int countForTask(int taskId);
}
