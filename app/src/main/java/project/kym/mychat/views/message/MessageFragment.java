package project.kym.mychat.views.message;


import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gun0912.tedpermission.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import project.kym.mychat.R;
import project.kym.mychat.databinding.FragmentMessageBinding;
import project.kym.mychat.util.PermissionUtil;
import project.kym.mychat.util.RLog;
import project.kym.mychat.views.OnShowKeyboardListener;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class MessageFragment extends Fragment implements MessageContract.View, OnShowKeyboardListener {
    private static final int PICK_FROM_ALBUM = 10;
    public MessageFragment() {}

    private FragmentMessageBinding binding;
    private MessagePresenter presenter;
    private MessageRecyclerViewAdapter messageRecyclerViewAdapter;

    // 에디트 텍스트가 클릭될때 어뎁터에 표시된 마지막 아이템 포지션
    private int lastVisiblePostionWhenEditTextClicked = 0;  // 이 값을 통해 키보드가 올라올 당시 스크롤 이벤트를 발생시킬지 여부 결정

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageRecyclerViewAdapter = new MessageRecyclerViewAdapter();
        presenter = new MessagePresenter(this);
        presenter.setAdapter(messageRecyclerViewAdapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false);
        binding.reclclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reclclerview.setAdapter(messageRecyclerViewAdapter);
        if(getArguments() != null){
            presenter.init(getArguments());
        } else {
            presenter.init(getActivity().getIntent().getExtras());
        }


        binding.submitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onSendButtonClicked(binding.submitText.getText().toString());
            }
        });


        // 상단에 날짜 표시 애니메이션 설정
        final Animation fadein = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);
        final Animation fadeout = AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);
        binding.reclclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                RLog.i("채팅방 스크롤 상태 "  +  newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.date.setAnimation(fadeout);
                    fadeout.start();
                    binding.date.setVisibility(View.INVISIBLE);
                } else if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    if (binding.date.getVisibility() != View.VISIBLE) {
                        binding.date.setAnimation(fadein);
                        fadein.start();
                    }
                    binding.date.setVisibility(View.VISIBLE);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 15 || dy % 3 == 0 || dy % 5 == 0) {
                    int firstCompletelyVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    presenter.listenFirstVisiblePosition(firstCompletelyVisibleItemPosition);
                }

            }
        });

        binding.submitText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    lastVisiblePostionWhenEditTextClicked = ((LinearLayoutManager) binding.reclclerview.getLayoutManager()).findLastCompletelyVisibleItemPosition();
//                    RLog.i(lastVisiblePostionWhenEditTextClicked + "");
                }
                return false;
            }
        });

        //확장메뉴 세팅
        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.chatAdditionLayout.getVisibility() == View.VISIBLE){
                    binding.addBtn.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_add_black_24dp));
                    binding.chatAdditionLayout.setVisibility(View.GONE);
                } else {
                    binding.addBtn.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_cancel_black_24dp));
                    binding.chatAdditionLayout.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.submitText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }
            }
        });

        binding.addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtil.requestStoragePermissions(getContext(), new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        startPickFromAlbumActivity();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {

                    }
                });
            }
        });

        //키보드 이벤트 제어
//        binding.reclclerview.getRootView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                Rect r = new Rect();
//                binding.getRoot().getWindowVisibleDisplayFrame(r);
//                int heightDiff = binding.getRoot().getRootView().getHeight() - (r.bottom - r.top);
//                if(!isKeyboardVisible){
//                    if (heightDiff > 400) { // if more than 100 pixels, its probably a keyboard...
//                        isKeyboardVisible = true;
//                        int lastItemPosition = binding.reclclerview.getAdapter().getItemCount() - 1;
//                        if(lastItemPosition > -1 && lastItemPosition == lastVisiblePostionWhenEditTextClicked)
//                            binding.reclclerview.smoothScrollToPosition(lastItemPosition);
//                    }
//
//                } else {
//                    if(heightDiff < 300){
//                        isKeyboardVisible = false;
//                    }
//                }
////                RLog.i("heightDiff " + heightDiff);
//            }
//        });

        //텍스트가 있을 경우에만 버튼 활성화화
        binding.submitText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                RLog.i(charSequence.toString());
                if(charSequence.toString().isEmpty())
                    binding.inActivateSubmitButton.setVisibility(View.VISIBLE);
                else
                    binding.inActivateSubmitButton.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable editable) {}
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        presenter.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        presenter.onStop();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == PICK_FROM_ALBUM){
                List<Uri> uris = new ArrayList<>();
                if(data.getData()!=null){
                    uris.add(data.getData());
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            uris.add(uri);
                        }
                    }
                }

                presenter.onPhotoSelected(getContext(), uris);
            }
        } else {
//            finish();
        }
    }

    @Override
    public void onDestroy() {
        RLog.i();
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void setTitlebar(String title) {
        RLog.i("타이틀 수정: " + title);
        getActivity().setTitle(title);
    }

    @Override
    public void scrollToLastPosition(boolean forced) {
        int lastItemPosition = binding.reclclerview.getAdapter().getItemCount() - 1;
        if(forced){
            binding.reclclerview.scrollToPosition(lastItemPosition);
        } else {
            lastVisiblePostionWhenEditTextClicked = ((LinearLayoutManager) binding.reclclerview.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if(lastItemPosition > -1 && lastItemPosition - lastVisiblePostionWhenEditTextClicked < 3)
                binding.reclclerview.scrollToPosition(lastItemPosition);
        }

    }

    @Override
    public void scrollToPosition(int position) {
        binding.reclclerview.smoothScrollToPosition(position);
    }

    @Override
    public void setSendButtonEnabled(boolean enabled) {
//        binding.submitText.setEnabled(enabled); // 이거 유효한가..?
        binding.submitbutton.setEnabled(enabled);
        if(enabled){
//            binding.inActivateSubmitButton.setVisibility(View.GONE);
        }else {
            binding.inActivateSubmitButton.setVisibility(View.VISIBLE);
            binding.submitText.setText("");
        }
    }

    @Override
    public void clearEditText() {
        binding.submitText.setText("");
    }

    @Override
    public void setDateTextView(String text) {
        binding.date.setText(text);

    }

    @Override
    public void removeNotification() {
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);
    }

    @Override
    public void showProgress(boolean b) {
        if (b)
            binding.progress.setVisibility(View.VISIBLE);
        else
            binding.progress.setVisibility(View.GONE);

        setSendButtonEnabled(!b);
    }

    @Override
    public void onShow() {
        RLog.i();
        int lastItemPosition = binding.reclclerview.getAdapter().getItemCount() - 1;
        if(lastItemPosition > -1 && lastItemPosition == lastVisiblePostionWhenEditTextClicked)
            binding.reclclerview.scrollToPosition(lastItemPosition);
    }

    @Override
    public void onHide() {
        RLog.i();
    }

    public void startPickFromAlbumActivity() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//        startActivityForResult(intent, PICK_FROM_ALBUM);

        Intent intent = new Intent();
        intent.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
//                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setAction(Intent.ACTION_PICK);
//                startActivityForResult(intent, PICK_FROM_ALBUM);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_photo)), PICK_FROM_ALBUM);
    }


}
