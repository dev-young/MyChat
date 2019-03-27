package project.kym.mychat.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** 채팅방 모델 */
public class ChatModel {
    private String roomUid;  // 채팅방 Uid
    private boolean isGroup = false; // 그룹채팅 여부
    private Map<String, Integer> users = new HashMap<>(); //채팅방의 유저들 <uid, 해당 유저가 읽지 않은 메시지 수>
    private Map<String, String> lastRead = new HashMap<>(); //채팅방의 유저들 <uid, 해당 유저가 읽은 마지막 메시지>

    private String title;  //방 이름
    private String uid;  //작성자
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public static class Comment {
        private String uid;  //작성자
        private int type; // 메시지 타입
        private String message;  // 내용
        private String fileName;  // 파일 이름
        private String fileUrl;  // 파일 경로
        @ServerTimestamp
        private Date timestamp;    // 작성시간
        private Map<String,Object> readUsers = new HashMap<>();  //읽은 유저 목록

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
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

        public Map<String, Object> getReadUsers() {
            return readUsers;
        }

        public void setReadUsers(Map<String, Object> readUsers) {
            this.readUsers = readUsers;
        }

        @Override
        public String toString() {
            return "Comment{" +
                    "uid='" + uid + '\'' +
                    ", type=" + type +
                    ", message='" + message + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", fileUrl='" + fileUrl + '\'' +
                    ", timestamp=" + timestamp +
                    ", readUsers=" + readUsers +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ChatModel{" +
                "roomUid='" + roomUid + '\'' +
                ", isGroup=" + isGroup +
                ", users=" + users +
                ", uid='" + uid + '\'' +
                ", type=" + type +
                ", message='" + message + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
