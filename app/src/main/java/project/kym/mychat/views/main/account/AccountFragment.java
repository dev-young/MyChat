package project.kym.mychat.views.main.account;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import project.kym.mychat.R;
import project.kym.mychat.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment implements AccountViewContract{

    FragmentAccountBinding binding;
    UserModelViewModel userModelViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        userModelViewModel = new UserModelViewModel(this);
        binding.setViewModel(userModelViewModel);
        userModelViewModel.loadUserInfo();

        return binding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_comment,null);
        final EditText editText = (EditText) view.findViewById(R.id.commentDialog_edittext);
        builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Map<String,Object> stringObjectMap = new HashMap<>();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                stringObjectMap.put("comment",editText.getText().toString());
                FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(stringObjectMap);

            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }
}
