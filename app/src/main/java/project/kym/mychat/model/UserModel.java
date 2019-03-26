package project.kym.mychat.model;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    private String userName;
    private String profileImageUrl;
    private String uid;
    private String comment;
    private String pushToken;
    private String groupUid; // 현재 선택된 그룹 uid
    private Map<String, Boolean> groupMap = new HashMap<>(); // 그룹 목록 <그룹Uid, true>

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public Map<String, Boolean> getGroupMap() {
        return groupMap;
    }

    public void setGroupMap(Map<String, Boolean> groupMap) {
        this.groupMap = groupMap;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "userName='" + userName + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", uid='" + uid + '\'' +
                ", comment='" + comment + '\'' +
                ", pushToken='" + pushToken + '\'' +
                ", groupUid='" + groupUid + '\'' +
                ", groupMap=" + groupMap +
                '}';
    }
}
