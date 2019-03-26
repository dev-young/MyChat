package project.kym.mychat.model;

public class NotificationModel {
    public String to;
    public Data data;

    public NotificationModel(Data data) {
        this.data = data;
    }

    public static class MessageData extends Data{
        public String roomUid;
        public String roomName;
        public String photoUrl;
        public boolean isGroup;

        public MessageData() {
            type = Type.MESSAGE;
        }
    }

    public static class Data{
        public String type;
        public String title;
        public String text;
    }

    interface Type{
        String MESSAGE = "Message";
        String ALTER = "Alter";
    }

}
