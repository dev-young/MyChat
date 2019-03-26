package project.kym.mychat.views.main.people;

public interface PeopleListViewContract {

    void startDoubleMessageActivity(String destinationUID, String title);

    void startSelectFriendActivity();
}
