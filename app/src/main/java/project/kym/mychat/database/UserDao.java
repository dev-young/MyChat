package project.kym.mychat.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import project.kym.mychat.model.UserModel;

@Dao
public interface UserDao {

    @Query("SELECT * FROM usermodel")
    List<UserModel> getAll();


    @Query("DELETE FROM usermodel")
    int deleteAll();

    @Query("SELECT * FROM usermodel WHERE uid IN (:uid)")
    List<UserModel> loadAllByIds(int[] uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserModel users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll2(UserModel... users);

    @Delete
    void delete(UserModel user);
}
