package project.kym.mychat.views.main.people.adapter;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import project.kym.mychat.R;
import project.kym.mychat.databinding.ItemFriendBinding;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.views.main.people.PeopleListViewContract;

public class PeopleRecyclerAdapter extends RecyclerView.Adapter<PeopleRecyclerAdapter.ViewHolder>{

    List<UserModel> userModels;
    public PeopleListViewContract view;

    public PeopleRecyclerAdapter(PeopleListViewContract contract) {
        userModels = new ArrayList<>();
        this.view = contract;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemFriendBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_friend, viewGroup, false);
        binding.setViewModel(new PeopleViewModel(view));
        return new ViewHolder(binding.getRoot(), binding.getViewModel());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final UserModel userModel = userModels.get(position);
        holder.setItem(userModel);
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public void addItems(boolean isClear, List<UserModel> models) {
        if (isClear)
            userModels.clear();
        userModels.addAll(models);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private PeopleViewModel viewModel;

        public ViewHolder(View view, PeopleViewModel viewModel) {
            super(view);
            this.viewModel = viewModel;
        }

        public void setItem(UserModel item) {
            viewModel.loadItem(item);
        }

    }

}