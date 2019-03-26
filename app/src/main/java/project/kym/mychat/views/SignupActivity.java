package project.kym.mychat.views;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import project.kym.mychat.R;
import project.kym.mychat.util.RLog;
import project.kym.mychat.model.UserModel;

public class SignupActivity extends BaseActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private String splash_background;
    private ImageView profile;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }


        profile = (ImageView) findViewById(R.id.signupActivity_imageview_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        email = (EditText) findViewById(R.id.signupActivity_edittext_email);
        name = (EditText) findViewById(R.id.signupActivity_edittext_name);
        password = (EditText) findViewById(R.id.signupActivity_edittext_password);
        signup = (Button) findViewById(R.id.signupActivity_button_signup);
        signup.setBackgroundColor(Color.parseColor(splash_background));


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!checkInput()){
                    return;
                }

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                RLog.e("작업 취소!");
                            }
                        })
                        .addOnFailureListener(SignupActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                RLog.e("이건 실패다!" + e.getMessage());
                                Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                e.printStackTrace();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override /** FirebaseAuth 가입 성공 */
                            public void onSuccess(AuthResult authResult) {
                                final String uid = authResult.getUser().getUid();
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();
                                authResult.getUser().updateProfile(userProfileChangeRequest);

                                if(imageUri == null || imageUri.toString().isEmpty()){
                                    // 프사를 선택하지 않은 경우
                                    makeUserModelAndSendToServer("");
                                } else {
                                    // 프사를 선택한 경우
                                    final StorageReference reference = FirebaseStorage.getInstance().getReference().child("userImages").child(uid);
                                    UploadTask uploadTask = reference.putFile(imageUri);
                                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            if (task.isSuccessful()) {
                                                return task.getResult().getStorage().getDownloadUrl();
                                            } else
                                                throw task.getException();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override /** 이미지 저장 완료 */
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if(task.isSuccessful()){
                                                String imageUrl = task.getResult().toString();
                                                makeUserModelAndSendToServer(imageUrl);
                                            } else {
                                                // Handle failures
                                                RLog.e("이미지 저장 실패!");
                                                makeUserModelAndSendToServer("");
                                            }
                                        }
                                    });
                                }


                            }
                        });

            }
        });
    }

    private boolean checkInput() {
        if (email.getText().toString().isEmpty()
                || name.getText().toString().isEmpty()
                || password.getText().toString().isEmpty()
        ) {
            Toast.makeText(this, "공백이 없게 해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void makeUserModelAndSendToServer(String imageUrl){
        RLog.i("이미지 저장 완료!");
        RLog.e(imageUrl);
        UserModel userModel = new UserModel();
        userModel.setUserName(name.getText().toString());
        userModel.setProfileImageUrl(imageUrl);
        userModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());

        /** 파이어 스토어에 저장 */
        addToFireStore(userModel);
    }


    private void addToFireStore(UserModel userModel) {
        FirebaseFirestore.getInstance().collection("users").document(userModel.getUid()).set(userModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Deprecated
    private void addToRealtimeDB(UserModel userModel){
        FirebaseDatabase.getInstance().getReference().child("users").child(userModel.getUid()).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SignupActivity.this.finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            profile.setImageURI(data.getData()); // 가운데 뷰를 바꿈
            imageUri = data.getData();// 이미지 경로 원본
        }
    }
}
