package project.kym.mychat.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** 채팅방 모델 */
public class ChatModel {
    public String roomUid;  // 채팅방 Uid
    public boolean isGroup = false; // 그룹채팅 여부
    public Map<String, Integer> users = new HashMap<>(); //채팅방의 유저들 <uid, 해당 유저가 읽지 않은 메시지 수>

    public String title;  //방 이름
    public String uid;  //작성자
    public int type; // 메시지 타입
    public String message;  // 내용
    public String fileName;  // 파일 이름
    public String fileUrl;  // 파일 경로
    @ServerTimestamp
    public Date timestamp;    // 작성시간

    public static class Comment {
        public String uid;  //작성자
        public int type; // 메시지 타입
        public String message;  // 내용
        public String fileName;  // 파일 이름
        public String fileUrl;  // 파일 경로
        @ServerTimestamp
        public Date timestamp;    // 작성시간
        public Map<String,Object> readUsers = new HashMap<>();  //읽은 유저 목록

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
