package project.kym.mychat.database;

import java.util.Date;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import project.kym.mychat.model.ChatModel;
import project.kym.mychat.model.UserModel;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM comment")
    List<ChatModel.Comment> getAll();


    @Query("DELETE FROM comment")
    int deleteAll();

    @Query("SELECT * FROM comment WHERE roomUid = (:roomuid) order by timestamp")
    List<ChatModel.Comment> loadAllFrom(String roomuid);

    @Query("SELECT * FROM comment WHERE roomUid = (:roomuid) order by timestamp LIMIT :limit")
    List<ChatModel.Comment> loadAllFrom(String roomuid, int limit);

    @Query("SELECT * FROM comment order by timestamp")
    List<ChatModel.Comment> loadAllFrom();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChatModel.Comment users);

    @Query("UPDATE comment SET timestamp = (:date) WHERE uid = :uid")
    int update(String uid, long date);

    @Query("UPDATE comment SET localFilePath = (:localFilePath) WHERE uid = :uid")
    int update(String uid, String localFilePath);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long update(ChatModel.Comment comment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ChatModel.Comment... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<ChatModel.Comment> commentList);

    @Delete
    void delete(ChatModel.Comment comment);
}
