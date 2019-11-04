package project.kym.mychat.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import project.kym.mychat.model.ChatModel;
import project.kym.mychat.model.UserModel;

@Database(entities = {UserModel.class, ChatModel.Comment.class}, version = 3)
@TypeConverters(DateTypeConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract MessageDao messageDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "MyChat.db")
                            .fallbackToDestructiveMigration()   // 기존에 디비 삭제후 다시 저장하는 방식의 마이그레이션
                            .build();
                }
            }

        }
        return INSTANCE;
    }
}
