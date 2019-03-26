package project.kym.mychat.util;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import project.kym.mychat.R;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.views.main.people.adapter.PeopleRecyclerAdapter;

public class BindingUtil {

    @BindingAdapter({"imageUrl"})
    public static void loadProfileImage(final ImageView imageView, final String imageUrl) {
//         이미지는 Glide라는 라이브러리를 사용해 데이터를 설정한다
        if(imageUrl == null || imageUrl.isEmpty())
            return;

        Glide.with
                (imageView.getContext())
                .load(imageUrl)
                .apply(new RequestOptions().circleCrop().error(R.drawable.person_24dp).placeholder(R.drawable.person_24dp))
                .into(imageView);
    }

    @BindingAdapter("bind_UserModels")
    public static void setBindUserModels(RecyclerView view, List<UserModel> userModels) {
        PeopleRecyclerAdapter adapter = (PeopleRecyclerAdapter)view.getAdapter();
        adapter.addItems(true, userModels);
        adapter.notifyDataSetChanged();
    }
}
