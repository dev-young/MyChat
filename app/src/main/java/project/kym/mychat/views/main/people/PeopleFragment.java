package project.kym.mychat.views.main.people;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.kym.mychat.R;
import project.kym.mychat.databinding.FragmentPeopleBinding;
import project.kym.mychat.model.UserModel;
import project.kym.mychat.util.RLog;
import project.kym.mychat.views.SelectFriendActivity;
import project.kym.mychat.views.message.MessageActivity;
import project.kym.mychat.views.main.people.adapter.PeopleRecyclerAdapter;

public class PeopleFragment extends Fragment implements PeopleListViewContract{

    FragmentPeopleBinding binding;
    PeopleRecyclerAdapter peopleRecyclerAdapter;
    PeopleListViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        peopleRecyclerAdapter = new PeopleRecyclerAdapter(this);
        if(viewModel == null){
            viewModel = ViewModelProviders.of(this).get(PeopleListViewModel.class);
            viewModel.loadPeopleList();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        RLog.i();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_people, container, false);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        binding.recyclerview.setAdapter(peopleRecyclerAdapter);

        viewModel.setView(this);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        viewModel.isLoading.observe(this, isLoading -> {
            if(isLoading){
                binding.progress.setVisibility(View.VISIBLE);
            } else
                binding.progress.setVisibility(View.GONE);
        });

        viewModel.userModels.observe(this, userModels -> {
            peopleRecyclerAdapter.addItems(true, userModels);
            peopleRecyclerAdapter.notifyDataSetChanged();

            RLog.i(userModels.toString());
        });


        return binding.getRoot();
    }

    @Override
    public void startDoubleMessageActivity(String destinationUID, String title) {
        HashMap<String, Integer> roomUsers = new HashMap<>();
        roomUsers.put(destinationUID, 0);
        MessageActivity.start(getActivity(), null, roomUsers, false, title);
    }

    @Override
    public void startSelectFriendActivity() {
        startActivity(new Intent(binding.getRoot().getContext(), SelectFriendActivity.class));
    }
}
