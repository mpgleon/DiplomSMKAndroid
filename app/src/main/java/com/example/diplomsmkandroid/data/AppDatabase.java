package com.example.diplomsmkandroid.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.diplomsmkandroid.data.dao.*;
import com.example.diplomsmkandroid.data.entity.*;

@Database(entities = {
        UserEntity.class,
        FundEntity.class,
        ProjectEntity.class,
        DonationEntity.class,
        VolunteerTaskEntity.class,
        TestimonialEntity.class,
        SubscriberEntity.class,
        ReviewEntity.class,
        VolunteerApplicationEntity.class
}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract FundDao fundDao();
    public abstract ProjectDao projectDao();
    public abstract DonationDao donationDao();
    public abstract VolunteerTaskDao volunteerTaskDao();
    public abstract TestimonialDao testimonialDao();
    public abstract SubscriberDao subscriberDao();
    public abstract ReviewDao reviewDao();
    public abstract VolunteerApplicationDao volunteerApplicationDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "dobrovmeste.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
