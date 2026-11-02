package com.example.eggxtremeflight;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eggxtremeflight.databinding.FragmentLeadersBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LeadersFragment extends Fragment {
    private static final String TAG = "Leaders";

    private FragmentLeadersBinding binding;
    private LeadersAdapter adapter;
    private final UserRepository userRepository = new UserRepository();
    private final LeaderboardRepository leaderboardRepository = new LeaderboardRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLeadersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LeadersAdapter();
        binding.leadersRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.leadersRecycler.setAdapter(adapter);
        binding.leadersRetry.setOnClickListener(v -> loadLeaders());

        loadLeaders();
    }

    private void loadLeaders() {
        showLoading();

        // Сначала убеждаемся, что пользователь авторизован (правила Firestore этого требуют)
        userRepository.ensureSignedIn(new UserRepository.Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (binding == null) return; // экран уже закрыт
                leaderboardRepository.loadTopPlayers(new UserRepository.Callback<List<LeaderEntry>>() {
                    @Override
                    public void onSuccess(List<LeaderEntry> entries) {
                        if (binding == null) return;
                        Log.d(TAG, "Loaded " + entries.size() + " leaders");
                        showResult(entries, user.getUid());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Leaderboard query failed", e);
                        if (binding == null) return;
                        showStatus(R.string.leaders_error, true);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Sign-in failed", e);
                if (binding == null) return;
                showStatus(R.string.leaders_error, true);
            }
        });
    }

    private void showLoading() {
        binding.leadersProgress.setVisibility(View.VISIBLE);
        binding.leadersRecycler.setVisibility(View.GONE);
        binding.leadersStatusGroup.setVisibility(View.GONE);
    }

    private void showResult(List<LeaderEntry> entries, String currentUid) {
        if (entries.isEmpty()) {
            showStatus(R.string.leaders_empty, false);
            return;
        }
        adapter.submit(entries, currentUid);
        binding.leadersProgress.setVisibility(View.GONE);
        binding.leadersStatusGroup.setVisibility(View.GONE);
        binding.leadersRecycler.setVisibility(View.VISIBLE);
    }

    private void showStatus(int textRes, boolean canRetry) {
        binding.leadersProgress.setVisibility(View.GONE);
        binding.leadersRecycler.setVisibility(View.GONE);
        binding.leadersStatusText.setText(textRes);
        binding.leadersRetry.setVisibility(canRetry ? View.VISIBLE : View.GONE);
        binding.leadersStatusGroup.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}