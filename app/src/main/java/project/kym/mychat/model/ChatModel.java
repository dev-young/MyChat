package project.kym.mychat.model;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** 채팅방 모델 */
@Entity
//@IgnoreExtraProperties    // 이것과 @Exclude 를 게터에 선언하면 해당 필드는 파스에서 사용하지 않는다.
public class ChatModel {
    @PrimaryKey private String roomUid;  // 채팅방 Uid
    private boolean isGroup = false; // 그룹채팅 여부
    private Map<String, Integer> users = new HashMap<>(); //채팅방의 유저들 <userUid, 해당 유저가 읽지 않은 메시지 수>
    private Map<String, String> lastRead = new HashMap<>(); //채팅방의 유저들 <userUid, 해당 유저가 읽은 마지막 메시지>

    private String title;  //방 이름
    private String userUid;  //작성자
    private int type; // 메시지 타입
    private String message;  // 내용
    private String fileName;  // 파일 이름
    private String fileUrl;  // 파일 경로
    @ServerTimestamp
    private Date timestamp;    // 작성시간

    public String getRoomUid() {
        return roomUid;
    }

    public void setRoomUid(String roomUid) {
        this.roomUid = roomUid;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public Map<String, Integer> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Integer> users) {
        this.users = users;
    }

    public Map<String, String> getLastRead() {
        return lastRead;
    }

    public void setLastRead(Map<String, String> lastRead) {
        this.lastRead = lastRead;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Entity(primaryKeys = {"roomUid", "uid"}, indices = {@Index(value = {"roomUid", "timestamp"})})
    public static class Comment {
        public static final int TYPE_TEXT = 0;
        public static final int TYPE_PHOTO = 1;
        public static final int TYPE_FILE = 2;

        @NonNull private String roomUid;
        @NonNull private String uid;
        private String userUid;  //작성자
        private int type; // 메시지 타입
        private String message;  // 내용
        private String fileName;  // 파일 이름
        private String fileUrl;  // 파일 경로
        private String localFilePath;  // 파일 경로 (로컬)
        @ServerTimestamp
        private Date timestamp;    // 작성시간

        public String getRoomUid() {
            return roomUid;
        }

        public void setRoomUid(String roomUid) {
            this.roomUid = roomUid;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public String getLocalFilePath() {
            return localFilePath;
        }

        public void setLocalFilePath(String localFilePath) {
            this.localFilePath = localFilePath;
        }

        @Override
        public String toString() {
            return "Comment{" +
                    "roomUid='" + roomUid + '\'' +
                    ", userUid='" + userUid + '\'' +
                    ", type=" + type +
                    ", message='" + message + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", fileUrl='" + fileUrl + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }

        public void update(Comment newModel) {
            this.timestamp = newModel.timestamp;
        }
    }

    @Override
    public String toString() {
        return "ChatModel{" +
                "roomUid='" + roomUid + '\'' +
                ", isGroup=" + isGroup +
                ", users=" + users +
                ", userUid='" + userUid + '\'' +
                ", type=" + type +
                ", message='" + message + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
