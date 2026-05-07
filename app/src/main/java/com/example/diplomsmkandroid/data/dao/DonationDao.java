package com.example.diplomsmkandroid.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.diplomsmkandroid.data.entity.DonationEntity;

import java.util.List;

@Dao
public interface DonationDao {

    @Insert
    long insert(DonationEntity donation);

    @Query("SELECT * FROM donations WHERE user_id = :userId ORDER BY created_at DESC LIMIT 10")
    LiveData<List<DonationEntity>> getRecentByUser(int userId);

    @Query("SELECT * FROM donations WHERE user_id = :userId ORDER BY created_at DESC LIMIT 10")
    List<DonationEntity> getRecentByUserSync(int userId);

    @Query("SELECT * FROM donations WHERE project_id = :projectId ORDER BY created_at DESC")
    List<DonationEntity> getByProject(int projectId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM donations")
    double getTotalDonations();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM donations WHERE project_id = :projectId")
    double getTotalForProject(int projectId);

    @Query("SELECT COUNT(*) FROM donations WHERE project_id = :projectId")
    int countForProject(int projectId);
}
