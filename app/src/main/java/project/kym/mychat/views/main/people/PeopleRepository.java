package project.kym.mychat.views.main.people;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.RLog;

public class PeopleRepository {
    private static PeopleRepository instance;

    private PeopleRepository() {
    }

    public static PeopleRepository getInstance() {
        if(instance == null)
            instance = new PeopleRepository();
        return instance;
    }

    public void getPeopleList(final OnUserModelListListener onUserModelListListener){
//        getPeopleListFromRealTimeDB(onUserModelListListener);
        getPeopleListFromFireStore(onUserModelListListener);
    }

    private void getPeopleListFromFireStore(final OnUserModelListListener onUserModelListListener){
        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<UserModel> userModels = new ArrayList<>();
                        for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                            UserModel userModel = snapshot.toObject(UserModel.class);
                            if(userModel.getUid() != null && userModel.getUid().equals(myUid)){
                                continue;
                            }
                            userModels.add(userModel);
                        }
                        onUserModelListListener.onSuccess(userModels);
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        RLog.e(e.getMessage());
                        onUserModelListListener.onError(e.getMessage());
                    }
                });
    }

    @Deprecated
    private void getPeopleListFromRealTimeDB(final OnUserModelListListener onUserModelListListener){
        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<UserModel> userModels = new ArrayList<>();
                for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if(userModel.getUid() != null && userModel.getUid().equals(myUid)){
                        continue;
                    }
                    userModels.add(userModel);
                }
                onUserModelListListener.onSuccess(userModels);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                RLog.e(databaseError.getMessage());
                onUserModelListListener.onError(databaseError.getMessage());
            }
        });
    }

    public interface OnUserModelListListener{
        void onSuccess(List<UserModel> userModels);
        void onError(String errorMessage);
    }
}
